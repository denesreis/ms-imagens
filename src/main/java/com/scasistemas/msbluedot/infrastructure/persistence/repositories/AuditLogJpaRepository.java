package com.scasistemas.msbluedot.infrastructure.persistence.repositories;

import com.scasistemas.msbluedot.infrastructure.persistence.entities.AuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositório JPA para {@link AuditLogEntity}.
 *
 * <p>
 * Registros de auditoria são imutáveis — apenas inserção e consulta.
 * </p>
 */
public interface AuditLogJpaRepository extends JpaRepository<AuditLogEntity, Long> {

    List<AuditLogEntity> findByUsuario(String usuario);

    List<AuditLogEntity> findByAcao(String acao);

    List<AuditLogEntity> findByDataCriacaoBetween(LocalDateTime inicio, LocalDateTime fim);

    List<AuditLogEntity> findByIdEmpresa(Long idEmpresa);
}

