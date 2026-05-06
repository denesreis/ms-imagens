package com.scasistemas.msbluedot.infrastructure.persistence.repositories;

import com.scasistemas.msbluedot.infrastructure.persistence.entities.EmpresaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repositório JPA para {@link EmpresaEntity}.
 *
 * <p>
 * Os métodos de busca aplicam automaticamente a restrição de soft delete
 * via {@code @SQLRestriction("ativo = true")} na entidade.
 * </p>
 */
public interface EmpresaJpaRepository extends JpaRepository<EmpresaEntity, Long> {

    /**
     * Busca uma empresa pelo código ERP.
     * Filtra automaticamente por {@code ativo = true}.
     */
    Optional<EmpresaEntity> findByCodigoErp(String codigoErp);
}

