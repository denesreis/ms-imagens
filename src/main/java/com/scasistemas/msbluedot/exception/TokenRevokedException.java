package com.scasistemas.msbluedot.exception;

public class TokenRevokedException extends BusinessException {

    public TokenRevokedException() {
        super("Refresh token inválido, expirado ou já foi revogado");
    }

    public TokenRevokedException(String message) {
        super(message);
    }
}
