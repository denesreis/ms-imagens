package com.scasistemas.msbluedot.application.mappers;

import com.scasistemas.msbluedot.application.dto.empresa.EmpresaRequest;
import com.scasistemas.msbluedot.application.dto.empresa.EmpresaResponse;
import com.scasistemas.msbluedot.domain.entities.Empresa;
import com.scasistemas.msbluedot.infrastructure.persistence.entities.EmpresaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper MapStruct para conversão entre {@link Empresa} (domínio),
 * {@link EmpresaEntity} (JPA) e DTOs de apresentação.
 */
@Mapper(componentModel = "spring")
public interface EmpresaMapper {

    /** Converte entidade JPA → domínio. */
    Empresa toDomain(EmpresaEntity entity);

    /** Converte domínio → entidade JPA. */
    EmpresaEntity toEntity(Empresa domain);

    /** Converte domínio → DTO de resposta. */
    EmpresaResponse toResponse(Empresa empresa);

    /** Converte DTO de request → domínio (sem id e timestamps). */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ativo", ignore = true)
    @Mapping(target = "dataCriacao", ignore = true)
    @Mapping(target = "dataAtualizacao", ignore = true)
    Empresa fromRequest(EmpresaRequest request);
}

