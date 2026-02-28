package com.scasistemas.msimagens.domain.exceptions;

/**
 * Exceção lançada quando os dados fornecidos são inválidos para a regra de
 * negócio.
 *
 * <p>
 * Mapeada para HTTP 400 Bad Request.
 * </p>
 *
 * <p>
 * Exemplos de uso:
 * </p>
 * <ul>
 * <li>Código EAN com formato inválido</li>
 * <li>Tipo de arquivo não suportado para upload</li>
 * <li>Dados obrigatórios ausentes na requisição</li>
 * </ul>
 */
public class InvalidDataException extends BusinessException {

    public InvalidDataException(String message) {
        super(message);
    }

    /**
     * Cria mensagem indicando qual campo está inválido.
     *
     * @param campo  nome do campo inválido
     * @param motivo descrição do problema
     */
    public InvalidDataException(String campo, String motivo) {
        super("Campo inválido [" + campo + "]: " + motivo);
    }
}
