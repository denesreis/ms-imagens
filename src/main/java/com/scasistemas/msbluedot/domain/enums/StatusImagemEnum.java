package com.scasistemas.msbluedot.domain.enums;

/**
 * Rastreia o ciclo de vida do upload de uma imagem na Cloudflare Images API.
 *
 * <p>
 * Fluxo esperado:
 * </p>
 * 
 * <pre>
 *   PENDENTE → upload iniciado, aguardando confirmação da Cloudflare
 *   ATIVO    → upload concluído com sucesso, imagem disponível
 *   ERRO     → falha no upload, requer reenvio
 * </pre>
 */
public enum StatusImagemEnum {

    /**
     * Upload iniciado mas ainda não confirmado pela Cloudflare.
     * Estado inicial ao criar o registro de imagem.
     */
    PENDENTE,

    /**
     * Imagem enviada e disponível na Cloudflare com sucesso.
     */
    ATIVO,

    /**
     * Falha durante o upload. O campo detalhes pode conter a causa.
     */
    ERRO
}

