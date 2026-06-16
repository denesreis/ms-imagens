package com.scasistemas.msbluedot.cloudflare;

import com.scasistemas.msbluedot.config.ImageCompressionProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Set;

@Slf4j
@Service("newImageCompressionService")
@RequiredArgsConstructor
public class ImageCompressionService {

    private static final long MIN_SIZE_TO_COMPRESS_BYTES = 1_048_576L;
    private static final Set<String> SUPPORTED_TYPES = Set.of(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp");

    private final ImageCompressionProperties properties;

    public byte[] compress(byte[] imageBytes, String contentType, String filename) {
        if (!properties.isEnabled()) return imageBytes;
        if (imageBytes.length < MIN_SIZE_TO_COMPRESS_BYTES) return imageBytes;
        if (!isSupported(contentType)) return imageBytes;

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

    private String resolveOutputFormat(String contentType) {
        if (contentType == null) return "jpg";
        return switch (contentType.toLowerCase().trim()) {
            case "image/png" -> "png";
            case "image/gif" -> "gif";
            default -> "jpg";
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
