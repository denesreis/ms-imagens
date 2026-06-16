package com.scasistemas.msbluedot.cloudflare;

import com.scasistemas.msbluedot.cloudflare.dto.CloudflareTokenVerifyResponse;
import com.scasistemas.msbluedot.cloudflare.dto.CloudflareUploadImageResponse;
import com.scasistemas.msbluedot.config.CloudflareProperties;
import com.scasistemas.msbluedot.exception.CloudflareException;
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

@Slf4j
@Service("cloudflareImageClient")
public class CloudflareImageClient {

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
            @Qualifier("newImageCompressionService") ImageCompressionService compressionService) {
        this.cloudflareWebClient = cloudflareWebClient;
        this.cloudflareProperties = cloudflareProperties;
        this.compressionService = compressionService;
    }

    public boolean verifyToken(String token) {
        try {
            CloudflareTokenVerifyResponse response = cloudflareWebClient.get()
                    .uri("/user/tokens/verify")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, resp -> resp.bodyToMono(String.class)
                            .map(body -> (Throwable) new CloudflareException("Falha na verificação do token: " + body)))
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

    public CloudflareUploadResult uploadImage(byte[] imageBytes, String filename, String contentType) {
        try {
            byte[] processedBytes = compressionService.compress(imageBytes, contentType, filename);

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
                            .map(body -> (Throwable) new CloudflareException("Upload rejeitado pela API: " + body)))
                    .bodyToMono(CloudflareUploadImageResponse.class)
                    .block(TIMEOUT_UPLOAD);

            if (response == null || !response.isSuccess() || response.getResult() == null) {
                log.error("[Cloudflare] Resposta inválida no upload de '{}'", filename);
                return CloudflareUploadResult.error(filename, "Resposta inválida da API Cloudflare");
            }

            String imageId = response.getResult().getId();
            String url = extractPublicUrl(response.getResult().getVariants());
            log.info("[Cloudflare] Upload concluído - imageId: {}, filename: {}", imageId, filename);
            return CloudflareUploadResult.success(imageId, url, filename);

        } catch (CloudflareException e) {
            log.error("[Cloudflare] Erro da API no upload de '{}': {}", filename, e.getMessage());
            return CloudflareUploadResult.error(filename, e.getMessage());
        } catch (Exception e) {
            log.error("[Cloudflare] Erro inesperado no upload de '{}': {}", filename, e.getMessage(), e);
            return CloudflareUploadResult.error(filename, "Erro interno: " + e.getMessage());
        }
    }

    public boolean deleteImage(String imageId) {
        try {
            cloudflareWebClient.delete()
                    .uri("/accounts/{accountId}/images/v1/{imageId}",
                            cloudflareProperties.getAccountId(), imageId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + cloudflareProperties.getPostToken())
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, resp -> resp.bodyToMono(String.class)
                            .map(body -> (Throwable) new CloudflareException("Deleção rejeitada pela API: " + body)))
                    .bodyToMono(Void.class)
                    .retryWhen(Retry.backoff(RETRY_ATTEMPTS, RETRY_MIN_BACKOFF)
                            .maxBackoff(RETRY_MAX_BACKOFF)
                            .filter(ex -> !(ex instanceof CloudflareException)))
                    .block(TIMEOUT_DELETE);

            log.info("[Cloudflare] Imagem deletada - imageId: {}", imageId);
            return true;
        } catch (Exception e) {
            log.error("[Cloudflare] Erro ao deletar '{}': {}", imageId, e.getMessage());
            return false;
        }
    }

    private String extractPublicUrl(List<String> variants) {
        if (variants == null || variants.isEmpty()) return null;
        return variants.stream()
                .filter(v -> v.contains("/public"))
                .findFirst()
                .orElse(variants.get(0));
    }
}
