package com.scasistemas.msbluedot.exception;

public class CloudflareException extends BusinessException {

    public CloudflareException(String message) {
        super(message);
    }

    public CloudflareException(String message, Throwable cause) {
        super(message, cause);
    }

    public CloudflareException(int statusCode, String responseBody) {
        super("Erro na Cloudflare Images API [HTTP " + statusCode + "]: " + responseBody);
    }
}
