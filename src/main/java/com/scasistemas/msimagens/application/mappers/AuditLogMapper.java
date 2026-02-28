package com.scasistemas.msimagens.application.mappers;

import com.scasistemas.msimagens.domain.entities.AuditLog;
import com.scasistemas.msimagens.infrastructure.persistence.entities.AuditLogEntity;
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
