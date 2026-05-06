package com.scasistemas.msbluedot.infrastructure.cloudflare;

import com.scasistemas.msbluedot.config.CloudflareProperties;
import com.scasistemas.msbluedot.domain.exceptions.CloudflareException;
import com.scasistemas.msbluedot.domain.services.CloudflareUploadResult;
import com.scasistemas.msbluedot.domain.services.ICloudflareImageService;
import com.scasistemas.msbluedot.infrastructure.cloudflare.dto.CloudflareTokenVerifyResponse;
import com.scasistemas.msbluedot.infrastructure.cloudflare.dto.CloudflareUploadImageResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;

/**
 * Implementação do serviço de integração com a Cloudflare Images API.
 *
 * <p>
 * Usa {@link WebClient} (reativo) para as chamadas HTTP, fazendo a ponte
 * para o modelo imperativo da aplicação via {@code .block()}.
 * </p>
 *
 * <ul>
 * <li>verifyToken — GET /user/tokens/verify (retry automático)</li>
 * <li>uploadImage — POST /accounts/{id}/images/v1 (sem retry para evitar
 * duplicatas)</li>
 * <li>deleteImage — DELETE /accounts/{id}/images/v1/{imageId} (retry
 * automático)</li>
 * </ul>
 */
@Slf4j
@Service
public class CloudflareImageClient implements ICloudflareImageService {

    private static final int RETRY_ATTEMPTS = 3;
    private static final Duration RETRY_MIN_BACKOFF = Duration.ofSeconds(1);
    private static final Duration RETRY_MAX_BACKOFF = Duration.ofSeconds(10);
    private static final Duration TIMEOUT_VERIFY = Duration.ofSeconds(15);
    private static final Duration TIMEOUT_UPLOAD = Duration.ofSeconds(60);
    private static final Duration TIMEOUT_DELETE = Duration.ofSeconds(15);

    private final WebClient cloudflareWebClient;
    private final CloudflareProperties cloudflareProperties;
    private final ImageCompressionService compressionService;

    public CloudflareImageClient(
            @Qualifier("cloudflareWebClient") WebClient cloudflareWebClient,
            CloudflareProperties cloudflareProperties,
            ImageCompressionService compressionService) {
        this.cloudflareWebClient = cloudflareWebClient;
        this.cloudflareProperties = cloudflareProperties;
        this.compressionService = compressionService;
    }

    // ─────────────────────────────────────────────────────────────────
    // verifyToken
    // ─────────────────────────────────────────────────────────────────

    /**
     * Verifica se um token Cloudflare está ativo.
     *
     * <p>
     * Faz retry automático em erros de rede (não retenta erros 4xx/5xx da API).
     * </p>
     *
     * @param token Bearer token a ser verificado
     * @return {@code true} se o token é válido e status == "active"
     */
    @Override
    public boolean verifyToken(String token) {
        try {
            CloudflareTokenVerifyResponse response = cloudflareWebClient.get()
                    .uri("/user/tokens/verify")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, resp -> resp.bodyToMono(String.class)
                            .map(body -> (Throwable) new CloudflareException(
                                    "Falha na verificação do token: " + body)))
                    .bodyToMono(CloudflareTokenVerifyResponse.class)
                    .retryWhen(Retry.backoff(RETRY_ATTEMPTS, RETRY_MIN_BACKOFF)
                            .maxBackoff(RETRY_MAX_BACKOFF)
                            .filter(ex -> !(ex instanceof CloudflareException)))
                    .block(TIMEOUT_VERIFY);

            boolean valid = response != null
                    && response.isSuccess()
                    && response.getResult() != null
                    && "active".equalsIgnoreCase(response.getResult().getStatus());

            log.info("[Cloudflare] Token verificado - válido: {}", valid);
            return valid;

        } catch (CloudflareException e) {
            log.warn("[Cloudflare] Token inválido: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("[Cloudflare] Erro ao verificar token: {}", e.getMessage());
            return false;
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // uploadImage
    // ─────────────────────────────────────────────────────────────────

    /**
     * Faz upload de uma imagem para a Cloudflare Images API.
     *
     * <p>
     * A imagem é opcionalmente comprimida antes do envio. Em caso de falha,
     * retorna um {@link CloudflareUploadResult} com {@code status = ERRO}
     * em vez de lançar exceção, permitindo que o caller persista o status
     * na entidade {@code Imagem}.
     * </p>
     *
     * <p>
     * <b>Sem retry</b> — para evitar uploads duplicados em caso de falha parcial.
     * </p>
     *
     * @param imageBytes  bytes da imagem
     * @param filename    nome do arquivo
     * @param contentType tipo MIME (ex: image/jpeg)
     * @return resultado com status ATIVO ou ERRO
     */
    @Override
    public CloudflareUploadResult uploadImage(byte[] imageBytes, String filename, String contentType) {
        try {
            // Comprimir/redimensionar antes do upload
            byte[] processedBytes = compressionService.compress(imageBytes, contentType, filename);

            // Montar o corpo multipart/form-data
            MultipartBodyBuilder builder = new MultipartBodyBuilder();
            builder.part("file", processedBytes)
                    .filename(filename)
                    .contentType(MediaType.parseMediaType(contentType));

            CloudflareUploadImageResponse response = cloudflareWebClient.post()
                    .uri("/accounts/{accountId}/images/v1", cloudflareProperties.getAccountId())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + cloudflareProperties.getPostToken())
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(builder.build()))
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, resp -> resp.bodyToMono(String.class)
                            .map(body -> (Throwable) new CloudflareException(
                                    "Upload rejeitado pela API: " + body)))
                    .bodyToMono(CloudflareUploadImageResponse.class)
                    .block(TIMEOUT_UPLOAD);

            if (response == null || !response.isSuccess() || response.getResult() == null) {
                log.error("[Cloudflare] Resposta inválida no upload de '{}'", filename);
                return CloudflareUploadResult.error(filename, "Resposta inválida da API Cloudflare");
            }

            String imageId = response.getResult().getId();
            String url = extractPublicUrl(response.getResult().getVariants());

            log.info("[Cloudflare] Upload concluído - imageId: {}, filename: {}, url: {}", imageId, filename, url);
            return CloudflareUploadResult.success(imageId, url, filename);

        } catch (CloudflareException e) {
            log.error("[Cloudflare] Erro da API no upload de '{}': {}", filename, e.getMessage());
            return CloudflareUploadResult.error(filename, e.getMessage());
        } catch (Exception e) {
            log.error("[Cloudflare] Erro inesperado no upload de '{}': {}", filename, e.getMessage(), e);
            return CloudflareUploadResult.error(filename, "Erro interno: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // deleteImage
    // ─────────────────────────────────────────────────────────────────

    /**
     * Remove uma imagem da Cloudflare Images API.
     *
     * <p>
     * Faz retry automático em erros de rede.
     * </p>
     *
     * @param imageId ID da imagem na Cloudflare
     * @return {@code true} se deletada com sucesso
     */
    @Override
    public boolean deleteImage(String imageId) {
        try {
            cloudflareWebClient.delete()
                    .uri("/accounts/{accountId}/images/v1/{imageId}",
                            cloudflareProperties.getAccountId(), imageId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + cloudflareProperties.getPostToken())
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, resp -> resp.bodyToMono(String.class)
                            .map(body -> (Throwable) new CloudflareException(
                                    "Deleção rejeitada pela API: " + body)))
                    .bodyToMono(Void.class)
                    .retryWhen(Retry.backoff(RETRY_ATTEMPTS, RETRY_MIN_BACKOFF)
                            .maxBackoff(RETRY_MAX_BACKOFF)
                            .filter(ex -> !(ex instanceof CloudflareException)))
                    .block(TIMEOUT_DELETE);

            log.info("[Cloudflare] Imagem deletada - imageId: {}", imageId);
            return true;

        } catch (CloudflareException e) {
            log.error("[Cloudflare] Erro da API ao deletar '{}': {}", imageId, e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("[Cloudflare] Erro inesperado ao deletar '{}': {}", imageId, e.getMessage());
            return false;
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────

    /**
     * Extrai a URL pública da lista de variantes Cloudflare.
     *
     * <p>
     * Prefere a variante que contém {@code /public} no path.
     * Se não encontrar, retorna a primeira variante disponível.
     * </p>
     */
    private String extractPublicUrl(List<String> variants) {
        if (variants == null || variants.isEmpty()) {
            return null;
        }
        return variants.stream()
                .filter(v -> v.contains("/public"))
                .findFirst()
                .orElse(variants.get(0));
    }
}

