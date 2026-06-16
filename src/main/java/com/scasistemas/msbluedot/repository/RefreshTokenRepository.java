package com.scasistemas.msbluedot.repository;

import com.scasistemas.msbluedot.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    boolean existsByTokenHash(String tokenHash);

    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revogado = true WHERE rt.idUsuario = :idUsuario AND rt.revogado = false")
    void revokeAllByIdUsuario(@Param("idUsuario") Long idUsuario);

    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiraEm < :now")
    void deleteByExpiraEmBefore(@Param("now") LocalDateTime now);
}
