package com.scasistemas.msimagens.infrastructure.persistence.repositories;

import com.scasistemas.msimagens.infrastructure.persistence.entities.ProdutoEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repositório JPA para {@link ProdutoEntity}.
 *
 * <p>
 * Os métodos de busca aplicam automaticamente a restrição de soft delete
 * via {@code @SQLRestriction("ativo = true")} na entidade.
 * </p>
 */
public interface ProdutoJpaRepository extends JpaRepository<ProdutoEntity, String> {

    /**
     * Busca produtos pelo código EAN-13.
     * Filtra automaticamente por {@code ativo = true}.
     */
    List<ProdutoEntity> findByCodigoEan(String codigoEan);

    /** Verifica duplicidade de EAN dentro da mesma empresa. */
    boolean existsByCodigoEanAndIdEmpresa(String codigoEan, Long idEmpresa);

    /** Verifica duplicidade de EAN para produtos sem empresa (globais). */
    boolean existsByCodigoEanAndIdEmpresaIsNull(String codigoEan);

    /**
     * Lista produtos ativos de uma empresa com paginação.
     */
    Page<ProdutoEntity> findByIdEmpresa(Long idEmpresa, Pageable pageable);
}
