package com.scasistemas.msbluedot.domain.exceptions;

/**
 * Exceção lançada quando um recurso solicitado não é encontrado.
 *
 * <p>
 * Mapeada para HTTP 404 Not Found.
 * </p>
 *
 * <p>
 * Exemplos de uso:
 * </p>
 * <ul>
 * <li>Produto não encontrado pelo ID</li>
 * <li>Usuário não encontrado pelo nome</li>
 * <li>Imagem não encontrada pelo EAN</li>
 * </ul>
 */
public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * Cria a mensagem automaticamente no formato: "Recurso não encontrado: {id}".
     *
     * @param recurso nome do recurso (ex: "Produto", "Usuario")
     * @param id      identificador buscado
     */
    public ResourceNotFoundException(String recurso, Object id) {
        super(recurso + " não encontrado: " + id);
    }
}

