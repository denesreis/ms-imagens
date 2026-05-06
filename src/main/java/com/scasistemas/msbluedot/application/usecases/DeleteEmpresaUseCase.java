package com.scasistemas.msbluedot.application.usecases;

import com.scasistemas.msbluedot.domain.entities.AuditLog;
import com.scasistemas.msbluedot.domain.exceptions.ResourceNotFoundException;
import com.scasistemas.msbluedot.domain.repositories.IAuditLogRepository;
import com.scasistemas.msbluedot.domain.repositories.IEmpresaRepository;
import com.scasistemas.msbluedot.infrastructure.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use Case para exclusão (soft delete) de empresa (apenas ADMINISTRADOR).
 * O @SQLDelete na entidade JPA garante que ativo=false é aplicado no banco.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeleteEmpresaUseCase {

    private final IEmpresaRepository empresaRepository;
    private final IAuditLogRepository auditLogRepository;

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Transactional
    public void execute(Long id) {
        log.info("[DeleteEmpresa] Soft delete empresa id={}", id);

        if (!empresaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Empresa não encontrada: " + id);
        }

        empresaRepository.deleteById(id);

        auditLogRepository.save(AuditLog.builder()
                .acao("DELETAR_EMPRESA")
                .usuario(SecurityUtils.getCurrentUsername())
                .idEmpresa(id)
                .detalhes("Empresa id=" + id + " desativada (soft delete)")
                .build());

        log.info("[DeleteEmpresa] Empresa id={} desativada com sucesso", id);
    }
}

