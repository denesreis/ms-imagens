package com.scasistemas.msbluedot.repository;

import com.scasistemas.msbluedot.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByUsuario(String usuario);

    List<AuditLog> findByAcao(String acao);

    List<AuditLog> findByDataCriacaoBetween(LocalDateTime inicio, LocalDateTime fim);

    List<AuditLog> findByIdEmpresa(Long idEmpresa);
}
