package com.scasistemas.msbluedot.infrastructure.persistence;

import com.scasistemas.msbluedot.application.mappers.AuditLogMapper;
import com.scasistemas.msbluedot.domain.entities.AuditLog;
import com.scasistemas.msbluedot.domain.repositories.IAuditLogRepository;
import com.scasistemas.msbluedot.infrastructure.persistence.repositories.AuditLogJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Adapter que implementa {@link IAuditLogRepository} usando JPA.
 */
@Component
@RequiredArgsConstructor
public class AuditLogRepositoryAdapter implements IAuditLogRepository {

    private final AuditLogJpaRepository jpaRepository;
    private final AuditLogMapper mapper;

    @Override
    public AuditLog save(AuditLog auditLog) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(auditLog)));
    }

    @Override
    public List<AuditLog> findByUsuario(String usuario) {
        return jpaRepository.findByUsuario(usuario).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<AuditLog> findByAcao(String acao) {
        return jpaRepository.findByAcao(acao).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<AuditLog> findByDateRange(LocalDateTime inicio, LocalDateTime fim) {
        return jpaRepository.findByDataCriacaoBetween(inicio, fim).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<AuditLog> findByIdEmpresa(Long idEmpresa) {
        return jpaRepository.findByIdEmpresa(idEmpresa).stream()
                .map(mapper::toDomain)
                .toList();
    }
}

