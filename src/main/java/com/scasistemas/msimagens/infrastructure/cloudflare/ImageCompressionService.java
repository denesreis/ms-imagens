package com.scasistemas.msimagens.infrastructure.cloudflare;

import com.scasistemas.msimagens.config.ImageCompressionProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Set;

/**
 * Serviço de compressão e redimensionamento de imagens usando Thumbnailator.
 *
 * <p>
 * Reduz o tamanho das imagens antes do envio à Cloudflare, diminuindo
 * consumo de bandwidth e tempo de upload.
 * </p>
 *
 * <p>
 * Comportamento:
 * <ul>
 * <li>Compressão desabilitada via {@code app.image.compression.enabled=false}:
 * retorna bytes originais</li>
 * <li>Arquivo menor que {@code MIN_SIZE_TO_COMPRESS_BYTES} (1 MB): retorna
 * bytes originais</li>
 * <li>Tipo MIME não suportado: retorna bytes originais</li>
 * <li>Falha na compressão: log de aviso e retorna bytes originais
 * (fail-safe)</li>
 * </ul>
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ImageCompressionService {

    /** Tamanho mínimo para ativar compressão: 1 MB */
    private static final long MIN_SIZE_TO_COMPRESS_BYTES = 1_048_576L;

    /** Tipos MIME suportados pelo Thumbnailator */
    private static final Set<String> SUPPORTED_TYPES = Set.of(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp");

    private final ImageCompressionProperties properties;

    /**
     * Comprime/redimensiona a imagem se necessário.
     *
     * @param imageBytes  bytes originais da imagem
     * @param contentType tipo MIME (ex: {@code image/jpeg})
     * @param filename    nome do arquivo (usado apenas no log)
     * @return bytes comprimidos ou bytes originais (nunca null)
     */
    public byte[] compress(byte[] imageBytes, String contentType, String filename) {
        if (!properties.isEnabled()) {
            log.debug("[Compressão] Desabilitada - retornando original para '{}'", filename);
            return imageBytes;
        }

        if (imageBytes.length < MIN_SIZE_TO_COMPRESS_BYTES) {
            log.debug("[Compressão] '{}' com {} KB - abaixo de 1 MB, pulando compressão",
                    filename, imageBytes.length / 1024);
            return imageBytes;
        }

        if (!isSupported(contentType)) {
            log.debug("[Compressão] Tipo '{}' não suportado para '{}', retornando original",
                    contentType, filename);
            return imageBytes;
        }

        try {
            byte[] compressed = doCompress(imageBytes, contentType);
            logReduction(filename, imageBytes.length, compressed.length);
            return compressed;
        } catch (Exception e) {
            log.warn("[Compressão] Falha ao comprimir '{}' ({}), usando arquivo original: {}",
                    filename, contentType, e.getMessage());
            return imageBytes;
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // Privados
    // ─────────────────────────────────────────────────────────────────

    private byte[] doCompress(byte[] imageBytes, String contentType) throws IOException {
        String outputFormat = resolveOutputFormat(contentType);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Thumbnails.of(new ByteArrayInputStream(imageBytes))
                .size(properties.getMaxWidth(), properties.getMaxHeight())
                .keepAspectRatio(true)
                .outputQuality(properties.getQuality())
                .outputFormat(outputFormat)
                .toOutputStream(out);

        return out.toByteArray();
    }

    /**
     * Mapeia o tipo MIME para o formato de saída do Thumbnailator.
     *
     * <p>
     * WebP é convertido para JPEG como fallback, pois o suporte de escrita
     * de WebP varia entre versões do ImageIO.
     * </p>
     */
    private String resolveOutputFormat(String contentType) {
        if (contentType == null)
            return "jpg";
        return switch (contentType.toLowerCase().trim()) {
            case "image/png" -> "png";
            case "image/gif" -> "gif";
            default -> "jpg"; // jpeg, webp → jpg
        };
    }

    private boolean isSupported(String contentType) {
        return contentType != null && SUPPORTED_TYPES.contains(contentType.toLowerCase().trim());
    }

    private void logReduction(String filename, long originalSize, long compressedSize) {
        double reductionPct = (1.0 - (double) compressedSize / originalSize) * 100.0;
        log.info("[Compressão] '{}' comprimido: {} MB → {} MB (redução de {}%)",
                filename,
                String.format("%.2f", originalSize / 1_048_576.0),
                String.format("%.2f", compressedSize / 1_048_576.0),
                String.format("%.1f", reductionPct));
    }
}
