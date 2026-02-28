package com.scasistemas.msimagens.application.mappers;

import com.scasistemas.msimagens.application.dto.usuario.UsuarioRequest;
import com.scasistemas.msimagens.application.dto.usuario.UsuarioResponse;
import com.scasistemas.msimagens.domain.entities.Usuario;
import com.scasistemas.msimagens.infrastructure.persistence.entities.UsuarioEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper MapStruct para conversão entre {@link Usuario} (domínio),
 * {@link UsuarioEntity} (JPA) e DTOs de apresentação.
 *
 * <p>
 * O campo {@code senha} é mapeado normalmente — a responsabilidade
 * de hash BCrypt é do use case, não do mapper.
 * </p>
 */
@Mapper(componentModel = "spring")
public interface UsuarioMapper {

    /** Converte entidade JPA → domínio. */
    Usuario toDomain(UsuarioEntity entity);

    /** Converte domínio → entidade JPA. */
    UsuarioEntity toEntity(Usuario domain);

    /** Converte domínio → DTO de resposta (sem senha). */
    UsuarioResponse toResponse(Usuario usuario);

    /** Converte DTO de request → domínio (sem id, tokens e timestamps). */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ativo", ignore = true)
    @Mapping(target = "tentativasLogin", ignore = true)
    @Mapping(target = "bloqueadoAte", ignore = true)
    @Mapping(target = "dataCriacao", ignore = true)
    @Mapping(target = "dataAtualizacao", ignore = true)
    Usuario fromRequest(UsuarioRequest request);
}
