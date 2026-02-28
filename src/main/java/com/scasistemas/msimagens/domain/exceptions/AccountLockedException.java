package com.scasistemas.msimagens.domain.exceptions;

import java.time.LocalDateTime;

/**
 * Exceção lançada quando uma conta está bloqueada por tentativas excessivas de
 * login.
 *
 * <p>
 * Mapeada para HTTP 423 Locked.
 * </p>
 *
 * <p>
 * O campo {@code bloqueadoAte} informa quando o bloqueio expira,
 * permitindo que a API retorne essa informação ao cliente.
 * </p>
 */
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

    /** Retorna quando o bloqueio expira. */
    public LocalDateTime getBloqueadoAte() {
        return bloqueadoAte;
    }
}
