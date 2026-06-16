package com.scasistemas.msbluedot.exception;

public class WhatsappException extends RuntimeException {

    public WhatsappException(String message) {
        super(message);
    }

    public WhatsappException(String message, Throwable cause) {
        super(message, cause);
    }
}
