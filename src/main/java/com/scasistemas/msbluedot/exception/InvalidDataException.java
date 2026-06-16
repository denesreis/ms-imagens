package com.scasistemas.msbluedot.exception;

public class InvalidDataException extends BusinessException {

    public InvalidDataException(String message) {
        super(message);
    }

    public InvalidDataException(String campo, String motivo) {
        super("Campo inválido [" + campo + "]: " + motivo);
    }
}
