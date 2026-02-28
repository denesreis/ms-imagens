package com.scasistemas.msimagens.domain.services;

/**
 * Interface de serviço de domínio para integração com a Cloudflare Images API.
 *
 * <p>
 * Segue o princípio de inversão de dependência (DIP) da Clean Architecture:
 * o domínio define o contrato e a infraestrutura
 * ({@link com.scasistemas.msimagens.infrastructure.cloudflare.CloudflareImageClient})
 * fornece a implementação.
 * </p>
 */
public interface ICloudflareImageService {

    /**
     * Verifica se o token de leitura da Cloudflare é válido e ativo.
     *
     * @param token token a ser verificado (normalmente {@code cloudflare.getToken})
     * @return {@code true} se o token é válido e está ativo
     */
    boolean verifyToken(String token);

    /**
     * Faz upload de uma imagem para a Cloudflare Images API.
     *
     * <p>
     * Antes do envio, a imagem pode ser comprimida/redimensionada pelo
     * {@link com.scasistemas.msimagens.infrastructure.cloudflare.ImageCompressionService}.
     * </p>
     *
     * @param imageBytes  bytes da imagem
     * @param filename    nome do arquivo (ex: {@code produto-001.jpg})
     * @param contentType tipo MIME da imagem (ex: {@code image/jpeg})
     * @return resultado do upload com ID, URL e status (ATIVO ou ERRO)
     */
    CloudflareUploadResult uploadImage(byte[] imageBytes, String filename, String contentType);

    /**
     * Remove uma imagem da Cloudflare Images API.
     *
     * @param imageId ID da imagem na Cloudflare (campo {@code idImagemCloudflare})
     * @return {@code true} se a imagem foi deletada com sucesso
     */
    boolean deleteImage(String imageId);
}
