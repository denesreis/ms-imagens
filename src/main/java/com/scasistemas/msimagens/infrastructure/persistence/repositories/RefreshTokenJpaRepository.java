package com.scasistemas.msimagens.infrastructure.persistence.repositories;

import com.scasistemas.msimagens.infrastructure.persistence.entities.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repositório JPA para {@link RefreshTokenEntity}.
 */
public interface RefreshTokenJpaRepository extends JpaRepository<RefreshTokenEntity, Long> {

    /**
     * Busca um refresh token pelo hash SHA-256.
     */
    Optional<RefreshTokenEntity> findByTokenHash(String tokenHash);

    /**
     * Revoga todos os refresh tokens ativos de um usuário.
     * Usado no logout ou ao detectar comprometimento de conta.
     */
    @Modifying
    @Query("UPDATE RefreshTokenEntity rt SET rt.revogado = true WHERE rt.idUsuario = :idUsuario AND rt.revogado = false")
    void revokeAllByIdUsuario(@Param("idUsuario") Long idUsuario);

    /**
     * Remove fisicamente tokens expirados para limpeza periódica da tabela.
     */
    @Modifying
    @Query("DELETE FROM RefreshTokenEntity rt WHERE rt.expiraEm < :now")
    void deleteByExpiraEmBefore(@Param("now") LocalDateTime now);

    /** Verifica se existe token com o hash informado. */
    boolean existsByTokenHash(String tokenHash);
}
