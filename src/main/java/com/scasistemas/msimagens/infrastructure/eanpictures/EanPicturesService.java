package com.scasistemas.msimagens.infrastructure.eanpictures;

import io.netty.resolver.DefaultAddressResolverGroup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.ClientCodecConfigurer;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.Optional;

/**
 * Serviço que consulta a API pública eanpictures.com.br para obter
 * a imagem de um produto a partir do código EAN.
 *
 * <p>
 * URL consultada:
 * {@code http://eanpictures.com.br:9000/api/gtin/{codigoEan}}
 * </p>
 * <p>
 * Retorno esperado: bytes de imagem (image/jpeg ou image/png).
 * </p>
 * <p>
 * Em caso de falha (4xx, 5xx, timeout, EAN não encontrado), retorna
 * {@link Optional#empty()}.
 * </p>
 */
@Slf4j
@Service
public class EanPicturesService {

    private static final String BASE_URL = "http://eanpictures.com.br:9000";
    /** Timeout de resposta: 60s — a API pode ser lenta para imagens grandes */
    private static final Duration TIMEOUT = Duration.ofSeconds(60);
    /** Limite de buffer: 10 MB (imagens podem ultrapassar o padrão de 256 KB) */
    private static final int MAX_BUFFER_BYTES = 10 * 1024 * 1024;

    private final WebClient webClient;

    public EanPicturesService() {
        // Usa o resolver de DNS do sistema (JVM) em vez do resolver interno do Reactor Netty,
        // pois o resolver nativo Netty pode falhar em ambientes Windows com DNS customizado.
        HttpClient httpClient = HttpClient.create()
                .resolver(DefaultAddressResolverGroup.INSTANCE)
                .responseTimeout(TIMEOUT);

        // Aumenta o limite de buffer para suportar imagens grandes (padrão é apenas 256 KB)
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs((ClientCodecConfigurer cfg) -> cfg.defaultCodecs().maxInMemorySize(MAX_BUFFER_BYTES))
                .build();

        this.webClient = WebClient.builder()
                .baseUrl(BASE_URL)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .exchangeStrategies(strategies)
                .build();
    }

    /**
     * Busca a imagem do produto via EAN na API eanpictures.com.br.
     *
     * @param codigoEan código EAN-13 do produto
     * @return {@link Optional} com os bytes e metadados da imagem, ou vazio se não
     *         encontrada
     */
    public Optional<EanPicturesResult> fetchImage(String codigoEan) {
        if (codigoEan == null || codigoEan.isBlank()) {
            return Optional.empty();
        }

        log.info("[EanPictures] Buscando imagem para EAN={}", codigoEan);

        try {
            byte[] bytes = webClient.get()
                    .uri("/api/gtin/{ean}", codigoEan)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, response -> {
                        log.warn("[EanPictures] EAN={} retornou HTTP {}", codigoEan, response.statusCode().value());
                        return response.createError();
                    })
                    .bodyToMono(byte[].class)
                    .timeout(TIMEOUT)
                    .block();

            if (bytes == null || bytes.length == 0) {
                log.info("[EanPictures] EAN={} sem conteúdo de imagem", codigoEan);
                return Optional.empty();
            }

            // detecta se é PNG (magic bytes 89 50 4E 47) ou assume jpeg
            String contentType = isPNG(bytes) ? MediaType.IMAGE_PNG_VALUE : MediaType.IMAGE_JPEG_VALUE;
            String ext = isPNG(bytes) ? ".png" : ".jpg";
            String filename = codigoEan + ext;

            log.info("[EanPictures] EAN={} imagem encontrada: {} bytes, {}", codigoEan, bytes.length, contentType);
            return Optional.of(new EanPicturesResult(bytes, contentType, filename));

        } catch (Exception ex) {
            log.warn("[EanPictures] Falha ao buscar imagem EAN={}: {}", codigoEan, ex.getMessage());
            return Optional.empty();
        }
    }

    private boolean isPNG(byte[] bytes) {
        return bytes.length >= 4
                && (bytes[0] & 0xFF) == 0x89
                && (bytes[1] & 0xFF) == 0x50
                && (bytes[2] & 0xFF) == 0x4E
                && (bytes[3] & 0xFF) == 0x47;
    }
}
