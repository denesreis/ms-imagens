package com.scasistemas.msimagens.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Propriedades de configuração da compressão de imagens.
 *
 * <p>
 * Mapeadas do prefixo <code>app.image.compression</code> no application.yml.
 * </p>
 *
 * <p>
 * Usa {@link net.coobird.thumbnailator.Thumbnails} para redimensionar e
 * comprimir imagens antes do upload na Cloudflare, reduzindo custo de
 * bandwidth e armazenamento.
 * </p>
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app.image.compression")
public class ImageCompressionProperties {

    /** Habilita/desabilita a compressão (padrão: true) */
    private boolean enabled = true;

    /** Largura máxima em pixels (padrão: 1920) */
    private int maxWidth = 1920;

    /** Altura máxima em pixels (padrão: 1080) */
    private int maxHeight = 1080;

    /**
     * Qualidade de compressão de 0.0 a 1.0.
     * 0.85 = boa qualidade com redução significativa de tamanho.
     */
    private double quality = 0.85;
}
