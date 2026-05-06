package com.scasistemas.msbluedot.domain.services;

import com.scasistemas.msbluedot.domain.enums.StatusImagemEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Resultado de uma operação de upload na Cloudflare Images API.
 *
 * <p>
 * Objeto de valor do domínio que encapsula o resultado do upload,
 * incluindo o status final da imagem ({@link StatusImagemEnum#ATIVO} ou
 * {@link StatusImagemEnum#ERRO}).
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CloudflareUploadResult {

    /** ID da imagem na Cloudflare (null em caso de erro) */
    private String imageId;

    /** URL pública da variante "public" da imagem (null em caso de erro) */
    private String url;

    /** Nome do arquivo enviado */
    private String filename;

    /** Status resultante: ATIVO se sucesso, ERRO se falha */
    private StatusImagemEnum status;

    /** Mensagem de erro (null se status == ATIVO) */
    private String errorMessage;

    /**
     * Cria um resultado de sucesso.
     *
     * @param imageId  ID da imagem na Cloudflare
     * @param url      URL pública da imagem
     * @param filename nome do arquivo
     * @return resultado com status ATIVO
     */
    public static CloudflareUploadResult success(String imageId, String url, String filename) {
        return CloudflareUploadResult.builder()
                .imageId(imageId)
                .url(url)
                .filename(filename)
                .status(StatusImagemEnum.ATIVO)
                .build();
    }

    /**
     * Cria um resultado de erro.
     *
     * @param filename     nome do arquivo que falhou
     * @param errorMessage descrição do erro
     * @return resultado com status ERRO
     */
    public static CloudflareUploadResult error(String filename, String errorMessage) {
        return CloudflareUploadResult.builder()
                .filename(filename)
                .status(StatusImagemEnum.ERRO)
                .errorMessage(errorMessage)
                .build();
    }

    /**
     * @return true se o upload foi bem-sucedido
     */
    public boolean isSuccess() {
        return StatusImagemEnum.ATIVO.equals(status);
    }
}

