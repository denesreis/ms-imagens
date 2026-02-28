package com.scasistemas.msimagens.application.mappers;

import com.scasistemas.msimagens.application.dto.imagem.ImagemProdutoResponse;
import com.scasistemas.msimagens.application.dto.imagem.ImagemResponse;
import com.scasistemas.msimagens.domain.entities.Imagem;
import com.scasistemas.msimagens.infrastructure.persistence.entities.ImagemEntity;
import org.mapstruct.Mapper;

/**
 * Mapper MapStruct para conversão entre {@link Imagem} (domínio),
 * {@link ImagemEntity} (JPA) e DTOs de apresentação.
 */
@Mapper(componentModel = "spring")
public interface ImagemMapper {

    /** Converte entidade JPA → domínio. */
    Imagem toDomain(ImagemEntity entity);

    /** Converte domínio → entidade JPA. */
    ImagemEntity toEntity(Imagem domain);

    /** Converte domínio → DTO de resposta completo. */
    ImagemResponse toResponse(Imagem imagem);

    /** Converte domínio → DTO simplificado para consultas por EAN/produto. */
    ImagemProdutoResponse toProdutoResponse(Imagem imagem);
}
