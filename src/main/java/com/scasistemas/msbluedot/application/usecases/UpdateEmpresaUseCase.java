package com.scasistemas.msbluedot.application.usecases;

import com.scasistemas.msbluedot.application.dto.empresa.EmpresaRequest;
import com.scasistemas.msbluedot.application.dto.empresa.EmpresaResponse;
import com.scasistemas.msbluedot.application.mappers.EmpresaMapper;
import com.scasistemas.msbluedot.domain.entities.AuditLog;
import com.scasistemas.msbluedot.domain.entities.Empresa;
import com.scasistemas.msbluedot.domain.exceptions.BusinessException;
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
 * Use Case para atualização de empresa (apenas ADMINISTRADOR).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateEmpresaUseCase {

    private final IEmpresaRepository empresaRepository;
    private final IAuditLogRepository auditLogRepository;
    private final EmpresaMapper empresaMapper;

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Transactional
    public EmpresaResponse execute(Long id, EmpresaRequest request) {
        log.info("[UpdateEmpresa] Atualizando empresa id={}", id);

        Empresa empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada: " + id));

        // Verificar duplicidade de codigoErp se foi alterado
        if (!empresa.getCodigoErp().equals(request.getCodigoErp())) {
            empresaRepository.findByCodigoErp(request.getCodigoErp()).ifPresent(e -> {
                throw new BusinessException("Já existe uma empresa com o código ERP: " + request.getCodigoErp());
            });
        }

        empresa.setCodigoErp(request.getCodigoErp());
        empresa.setNome(request.getNome());

        Empresa atualizada = empresaRepository.save(empresa);

        auditLogRepository.save(AuditLog.builder()
                .acao("ATUALIZAR_EMPRESA")
                .usuario(SecurityUtils.getCurrentUsername())
                .idEmpresa(id)
                .detalhes("Empresa atualizada: " + atualizada.getNome())
                .build());

        log.info("[UpdateEmpresa] Empresa id={} atualizada com sucesso", id);
        return empresaMapper.toResponse(atualizada);
    }
}

