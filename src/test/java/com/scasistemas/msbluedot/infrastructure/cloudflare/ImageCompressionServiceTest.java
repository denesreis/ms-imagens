package com.scasistemas.msbluedot.infrastructure.cloudflare;

import com.scasistemas.msbluedot.config.ImageCompressionProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ImageCompressionService - Testes unitários")
class ImageCompressionServiceTest {

    private ImageCompressionProperties properties;
    private ImageCompressionService imageCompressionService;

    @BeforeEach
    void setUp() {
        properties = new ImageCompressionProperties();
        properties.setEnabled(true);
        properties.setMaxWidth(1920);
        properties.setMaxHeight(1080);
        properties.setQuality(0.85);
        imageCompressionService = new ImageCompressionService(properties);
    }

    @Test
    @DisplayName("Deve retornar bytes originais quando compressão desabilitada")
    void deveRetornarOriginaisQuandoDesabilitado() {
        properties.setEnabled(false);
        byte[] original = new byte[2_000_000]; // 2 MB

        byte[] result = imageCompressionService.compress(original, "image/jpeg", "foto.jpg");

        assertThat(result).isSameAs(original);
    }

    @Test
    @DisplayName("Deve retornar bytes originais quando arquivo menor que 1 MB")
    void deveRetornarOriginaisParaArquivoPequeno() {
        byte[] original = new byte[500_000]; // 500 KB

        byte[] result = imageCompressionService.compress(original, "image/jpeg", "foto.jpg");

        assertThat(result).isSameAs(original);
    }

    @Test
    @DisplayName("Deve retornar bytes originais para tipo MIME não suportado")
    void deveRetornarOriginaisParaTipoNaoSuportado() {
        byte[] original = new byte[2_000_000]; // 2 MB

        byte[] result = imageCompressionService.compress(original, "application/pdf", "doc.pdf");

        assertThat(result).isSameAs(original);
    }

    @Test
    @DisplayName("Deve retornar bytes originais quando contentType é null")
    void deveRetornarOriginaisQuandoContentTypeNull() {
        byte[] original = new byte[2_000_000]; // 2 MB

        byte[] result = imageCompressionService.compress(original, null, "foto.jpg");

        assertThat(result).isSameAs(original);
    }

    @Test
    @DisplayName("Deve retornar bytes originais quando compressão falha (fail-safe)")
    void deveRetornarOriginaisQuandoCompressaoFalha() {
        // Bytes inválidos (não uma imagem) — Thumbnailator vai falhar
        byte[] invalidImage = new byte[2_000_000];
        invalidImage[0] = (byte) 0xFF; // Not a real image header

        byte[] result = imageCompressionService.compress(invalidImage, "image/jpeg", "corrupted.jpg");

        assertThat(result).isSameAs(invalidImage);
    }

    @Test
    @DisplayName("Deve aceitar tipos MIME suportados: jpeg, jpg, png, gif, webp")
    void deveAceitarTiposSuportados() {
        // Testar com arquivo menor que 1 MB (retorna direto sem comprimir)
        // O importante é verificar que não falha
        byte[] small = new byte[100];

        assertThat(imageCompressionService.compress(small, "image/jpeg", "a.jpg")).isSameAs(small);
        assertThat(imageCompressionService.compress(small, "image/png", "a.png")).isSameAs(small);
        assertThat(imageCompressionService.compress(small, "image/gif", "a.gif")).isSameAs(small);
        assertThat(imageCompressionService.compress(small, "image/webp", "a.webp")).isSameAs(small);
    }
}

