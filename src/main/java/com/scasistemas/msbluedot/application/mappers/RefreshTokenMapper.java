package com.scasistemas.msbluedot.application.mappers;

import com.scasistemas.msbluedot.domain.entities.RefreshToken;
import com.scasistemas.msbluedot.infrastructure.persistence.entities.RefreshTokenEntity;
import org.mapstruct.Mapper;

/**
 * Mapper MapStruct para conversão entre {@link RefreshToken} (domínio) e
 * {@link RefreshTokenEntity} (JPA/infraestrutura).
 */
@Mapper(componentModel = "spring")
public interface RefreshTokenMapper {

    /** Converte entidade JPA → domínio. */
    RefreshToken toDomain(RefreshTokenEntity entity);

    /** Converte domínio → entidade JPA. */
    RefreshTokenEntity toEntity(RefreshToken domain);
}

