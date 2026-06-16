package com.scasistemas.msbluedot.exception;

public class DuplicateUsernameException extends BusinessException {

    public DuplicateUsernameException(String nome) {
        super("Nome de usuário já está em uso: '" + nome + "'");
    }

    public DuplicateUsernameException(String message, Throwable cause) {
        super(message, cause);
    }
}
