package com.scasistemas.msbluedot.infrastructure.eanpictures;

/**
 * Resultado da busca de imagem na API eanpictures.com.br.
 *
 * @param bytes       bytes da imagem retornada
 * @param contentType MIME type (ex: image/jpeg)
 * @param filename    nome de arquivo sugerido (ean.jpg)
 */
public record EanPicturesResult(byte[] bytes, String contentType, String filename) {
}

