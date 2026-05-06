package com.scasistemas.msbluedot.domain.repositories;

import com.scasistemas.msbluedot.domain.entities.RefreshToken;

import java.util.Optional;

/**
 * Contrato de repositório para a entidade {@link RefreshToken}.
 *
 * <p>
 * Interface de domínio puro — sem dependência de frameworks.
 * A implementação está na camada de infraestrutura.
 * </p>
 */
public interface IRefreshTokenRepository {

    /** Persiste um novo refresh token. */
    RefreshToken save(RefreshToken refreshToken);

    /**
     * Busca um refresh token pelo hash SHA-256.
     *
     * @param tokenHash hash do token a localizar
     */
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    /**
     * Revoga todos os refresh tokens ativos de um usuário.
     * Usado no logout e ao detectar comprometimento de conta.
     *
     * @param idUsuario ID do usuário
     */
    void revokeAllByUsuarioId(Long idUsuario);

    /**
     * Remove fisicamente os tokens expirados do banco.
     * Executado por agendamento periódico para limpeza da tabela.
     */
    void deleteExpired();

    /** Verifica se existe token com o hash informado. */
    boolean existsByTokenHash(String tokenHash);
}

