package com.scasistemas.msbluedot.application.mappers;

import com.scasistemas.msbluedot.domain.entities.AuditLog;
import com.scasistemas.msbluedot.infrastructure.persistence.entities.AuditLogEntity;
import org.mapstruct.Mapper;

/**
 * Mapper MapStruct para conversão entre {@link AuditLog} (domínio) e
 * {@link AuditLogEntity} (JPA/infraestrutura).
 */
@Mapper(componentModel = "spring")
public interface AuditLogMapper {

    /** Converte entidade JPA → domínio. */
    AuditLog toDomain(AuditLogEntity entity);

    /** Converte domínio → entidade JPA. */
    AuditLogEntity toEntity(AuditLog domain);
}

