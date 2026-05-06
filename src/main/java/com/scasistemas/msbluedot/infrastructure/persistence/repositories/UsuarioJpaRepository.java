package com.scasistemas.msbluedot.infrastructure.persistence.repositories;

import com.scasistemas.msbluedot.infrastructure.persistence.entities.UsuarioEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Repositório JPA para {@link UsuarioEntity}.
 *
 * <p>
 * Os métodos de busca aplicam automaticamente a restrição de soft delete
 * via {@code @SQLRestriction("ativo = true")} na entidade.
 * </p>
 */
public interface UsuarioJpaRepository extends JpaRepository<UsuarioEntity, Long> {

    /**
     * Busca um usuário pelo nome (username para login), ignorando maiúsculas/minúsculas.
     * Filtra automaticamente por {@code ativo = true}.
     */
    Optional<UsuarioEntity> findByNomeIgnoreCase(String nome);

    /**
     * Lista usuários ativos de uma empresa com paginação.
     */
    Page<UsuarioEntity> findByIdEmpresa(Long idEmpresa, Pageable pageable);

    /**
     * Verifica existência de nome independente do soft delete.
     * Usado para validar a constraint UNIQUE antes de inserir.
     */
    @Query(value = "SELECT COUNT(*) > 0 FROM tb_usuario WHERE nome = :nome", nativeQuery = true)
    boolean existsByNomeIgnoringSoftDelete(@Param("nome") String nome);
}

