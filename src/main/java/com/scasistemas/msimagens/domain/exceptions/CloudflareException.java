package com.scasistemas.msimagens.domain.exceptions;

/**
 * Exceção lançada quando a integração com a Cloudflare Images API falha.
 *
 * <p>
 * Mapeada para HTTP 502 Bad Gateway.
 * </p>
 *
 * <p>
 * Exemplos de uso:
 * </p>
 * <ul>
 * <li>Timeout na conexão com a Cloudflare</li>
 * <li>Resposta de erro da API da Cloudflare (4xx/5xx)</li>
 * <li>Falha ao deletar imagem na Cloudflare</li>
 * </ul>
 */
public class CloudflareException extends BusinessException {

    public CloudflareException(String message) {
        super(message);
    }

    public CloudflareException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Cria exceção com o código de status HTTP retornado pela Cloudflare.
     *
     * @param statusCode   código HTTP retornado
     * @param responseBody corpo da resposta de erro
     */
    public CloudflareException(int statusCode, String responseBody) {
        super("Erro na Cloudflare Images API [HTTP " + statusCode + "]: " + responseBody);
    }
}
