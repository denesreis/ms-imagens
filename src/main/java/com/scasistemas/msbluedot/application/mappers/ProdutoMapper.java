package com.scasistemas.msbluedot.application.mappers;

import com.scasistemas.msbluedot.application.dto.produto.ProdutoRequest;
import com.scasistemas.msbluedot.application.dto.produto.ProdutoResponse;
import com.scasistemas.msbluedot.domain.entities.Produto;
import com.scasistemas.msbluedot.infrastructure.persistence.entities.ProdutoEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper MapStruct para conversão entre {@link Produto} (domínio),
 * {@link ProdutoEntity} (JPA) e DTOs de apresentação.
 */
@Mapper(componentModel = "spring")
public interface ProdutoMapper {

    /** Converte entidade JPA → domínio. */
    Produto toDomain(ProdutoEntity entity);

    /** Converte domínio → entidade JPA. */
    ProdutoEntity toEntity(Produto domain);

    /** Converte domínio → DTO de resposta. */
    ProdutoResponse toResponse(Produto produto);

    /** Converte DTO de request → domínio (sem id e timestamps). */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ativo", ignore = true)
    @Mapping(target = "dataCriacao", ignore = true)
    @Mapping(target = "dataAtualizacao", ignore = true)
    Produto fromRequest(ProdutoRequest request);
}

