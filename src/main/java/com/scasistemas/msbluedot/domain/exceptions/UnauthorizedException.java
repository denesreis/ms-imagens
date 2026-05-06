package com.scasistemas.msbluedot.domain.exceptions;

/**
 * Exceção lançada quando uma operação não é permitida para o usuário
 * autenticado.
 *
 * <p>
 * Mapeada para HTTP 403 Forbidden.
 * </p>
 *
 * <p>
 * Exemplos de uso:
 * </p>
 * <ul>
 * <li>Usuário tentando acessar recursos de outra empresa</li>
 * <li>Role insuficiente para a operação</li>
 * </ul>
 */
public class UnauthorizedException extends BusinessException {

    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException() {
        super("Acesso negado: permissão insuficiente para esta operação");
    }
}

