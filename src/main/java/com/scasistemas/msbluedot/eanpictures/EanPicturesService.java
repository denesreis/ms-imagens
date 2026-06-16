package com.scasistemas.msbluedot.eanpictures;

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

@Slf4j
@Service("eanPicturesService")
public class EanPicturesService {

    private static final String BASE_URL = "http://eanpictures.com.br:9000";
    private static final Duration TIMEOUT = Duration.ofSeconds(60);
    private static final int MAX_BUFFER_BYTES = 10 * 1024 * 1024;

    private final WebClient webClient;

    public EanPicturesService() {
        HttpClient httpClient = HttpClient.create()
                .resolver(DefaultAddressResolverGroup.INSTANCE)
                .responseTimeout(TIMEOUT);

        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs((ClientCodecConfigurer cfg) -> cfg.defaultCodecs().maxInMemorySize(MAX_BUFFER_BYTES))
                .build();

        this.webClient = WebClient.builder()
                .baseUrl(BASE_URL)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .exchangeStrategies(strategies)
                .build();
    }

    public Optional<EanPicturesResult> fetchImage(String codigoEan) {
        if (codigoEan == null || codigoEan.isBlank()) return Optional.empty();

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
