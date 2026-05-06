package com.scasistemas.msbluedot.domain.enums;

/**
 * Define a visibilidade de acesso das imagens armazenadas na Cloudflare.
 */
public enum TipoArmazenamentoEnum {

    /**
     * Imagem publicamente acessível via URL pública.
     * Consultável sem autenticação pelo endpoint GET /imagens/ean/{ean}.
     */
    ABERTO,

    /**
     * Imagem acessível apenas por URL assinada com token temporário.
     * Requer autenticação para geração do link de acesso.
     */
    PRIVADO
}

