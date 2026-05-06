package com.scasistemas.msbluedot.domain.exceptions;

/**
 * Exceção base para erros de regras de negócio da camada de domínio.
 *
 * <p>
 * Todas as exceções de negócio devem estender esta classe.
 * Mapeada para HTTP 422 Unprocessable Entity por padrão.
 * </p>
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}

