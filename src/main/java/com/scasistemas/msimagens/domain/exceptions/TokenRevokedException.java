package com.scasistemas.msimagens.domain.exceptions;

/**
 * Exceção lançada quando um refresh token já foi revogado ou não existe.
 *
 * <p>
 * Mapeada para HTTP 401 Unauthorized.
 * </p>
 *
 * <p>
 * Exemplos de uso:
 * </p>
 * <ul>
 * <li>Tentativa de usar refresh token após logout</li>
 * <li>Token inválido ou não encontrado no banco</li>
 * <li>Token expirado</li>
 * </ul>
 */
public class TokenRevokedException extends BusinessException {

    public TokenRevokedException() {
        super("Refresh token inválido, expirado ou já foi revogado");
    }

    public TokenRevokedException(String message) {
        super(message);
    }
}
