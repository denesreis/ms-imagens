package com.scasistemas.msbluedot.domain.exceptions;

/**
 * Exceção lançada ao tentar cadastrar um usuário com nome já existente.
 *
 * <p>
 * O campo {@code nome} em {@code tb_usuario} possui constraint UNIQUE.
 * Esta exceção é lançada antes de tentar persistir, na validação de negócio.
 * </p>
 *
 * <p>
 * Mapeada para HTTP 409 Conflict.
 * </p>
 */
public class DuplicateUsernameException extends BusinessException {

    public DuplicateUsernameException(String nome) {
        super("Nome de usuário já está em uso: '" + nome + "'");
    }

    public DuplicateUsernameException(String message, Throwable cause) {
        super(message, cause);
    }
}

