package com.scasistemas.msimagens.application.usecases;

import com.scasistemas.msimagens.application.dto.empresa.EmpresaRequest;
import com.scasistemas.msimagens.application.dto.empresa.EmpresaResponse;
import com.scasistemas.msimagens.application.mappers.EmpresaMapper;
import com.scasistemas.msimagens.domain.entities.AuditLog;
import com.scasistemas.msimagens.domain.entities.Empresa;
import com.scasistemas.msimagens.domain.exceptions.BusinessException;
import com.scasistemas.msimagens.domain.repositories.IAuditLogRepository;
import com.scasistemas.msimagens.domain.repositories.IEmpresaRepository;
import com.scasistemas.msimagens.infrastructure.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use Case para criação de empresa (apenas ADMINISTRADOR).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CreateEmpresaUseCase {

    private final IEmpresaRepository empresaRepository;
    private final IAuditLogRepository auditLogRepository;
    private final EmpresaMapper empresaMapper;

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Transactional
    public EmpresaResponse execute(EmpresaRequest request) {
        log.info("[CreateEmpresa] Criando empresa codigoErp='{}'", request.getCodigoErp());

        // Verificar duplicidade de codigoErp
        empresaRepository.findByCodigoErp(request.getCodigoErp()).ifPresent(e -> {
            throw new BusinessException("Já existe uma empresa com o código ERP: " + request.getCodigoErp());
        });

        Empresa empresa = empresaMapper.fromRequest(request);
        Empresa salva = empresaRepository.save(empresa);

        auditLogRepository.save(AuditLog.builder()
                .acao("CRIAR_EMPRESA")
                .usuario(SecurityUtils.getCurrentUsername())
                .idEmpresa(salva.getId())
                .detalhes("Empresa criada: " + salva.getNome())
                .build());

        log.info("[CreateEmpresa] Empresa id={} criada com sucesso", salva.getId());
        return empresaMapper.toResponse(salva);
    }
}
