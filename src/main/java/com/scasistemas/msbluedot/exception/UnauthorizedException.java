package com.scasistemas.msbluedot.exception;

public class UnauthorizedException extends BusinessException {

    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException() {
        super("Acesso negado: permissão insuficiente para esta operação");
    }
}
