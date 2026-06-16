package com.scasistemas.msbluedot.exception;

import java.time.LocalDateTime;

public class AccountLockedException extends BusinessException {

    private final LocalDateTime bloqueadoAte;

    public AccountLockedException(LocalDateTime bloqueadoAte) {
        super("Conta bloqueada temporariamente por excesso de tentativas de login. "
                + "Tente novamente após: " + bloqueadoAte);
        this.bloqueadoAte = bloqueadoAte;
    }

    public AccountLockedException(String message, LocalDateTime bloqueadoAte) {
        super(message);
        this.bloqueadoAte = bloqueadoAte;
    }

    public LocalDateTime getBloqueadoAte() {
        return bloqueadoAte;
    }
}
