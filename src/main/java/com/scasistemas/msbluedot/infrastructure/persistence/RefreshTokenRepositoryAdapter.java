package com.scasistemas.msbluedot.infrastructure.persistence;

import com.scasistemas.msbluedot.application.mappers.RefreshTokenMapper;
import com.scasistemas.msbluedot.domain.entities.RefreshToken;
import com.scasistemas.msbluedot.domain.repositories.IRefreshTokenRepository;
import com.scasistemas.msbluedot.infrastructure.persistence.repositories.RefreshTokenJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Adapter que implementa {@link IRefreshTokenRepository} usando JPA.
 */
@Component
@RequiredArgsConstructor
public class RefreshTokenRepositoryAdapter implements IRefreshTokenRepository {

    private final RefreshTokenJpaRepository jpaRepository;
    private final RefreshTokenMapper mapper;

    @Override
    public RefreshToken save(RefreshToken refreshToken) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(refreshToken)));
    }

    @Override
    public Optional<RefreshToken> findByTokenHash(String tokenHash) {
        return jpaRepository.findByTokenHash(tokenHash).map(mapper::toDomain);
    }

    @Override
    @Transactional
    public void revokeAllByUsuarioId(Long idUsuario) {
        jpaRepository.revokeAllByIdUsuario(idUsuario);
    }

    @Override
    @Transactional
    public void deleteExpired() {
        jpaRepository.deleteByExpiraEmBefore(LocalDateTime.now());
    }

    @Override
    public boolean existsByTokenHash(String tokenHash) {
        return jpaRepository.existsByTokenHash(tokenHash);
    }
}

