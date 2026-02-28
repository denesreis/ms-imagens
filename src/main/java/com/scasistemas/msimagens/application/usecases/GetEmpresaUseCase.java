package com.scasistemas.msimagens.application.usecases;

import com.scasistemas.msimagens.application.dto.empresa.EmpresaResponse;
import com.scasistemas.msimagens.application.mappers.EmpresaMapper;
import com.scasistemas.msimagens.domain.entities.Empresa;
import com.scasistemas.msimagens.domain.exceptions.ResourceNotFoundException;
import com.scasistemas.msimagens.domain.exceptions.UnauthorizedException;
import com.scasistemas.msimagens.domain.repositories.IEmpresaRepository;
import com.scasistemas.msimagens.infrastructure.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use Case para busca de empresa por ID.
 * ADMIN pode ver qualquer empresa; USUÁRIO só pode ver a sua própria.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GetEmpresaUseCase {

    private final IEmpresaRepository empresaRepository;
    private final EmpresaMapper empresaMapper;

    @Transactional(readOnly = true)
    public EmpresaResponse execute(Long id) {
        log.debug("[GetEmpresa] Buscando empresa id={}", id);

        Empresa empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada: " + id));

        // USUÁRIO só pode ver a empresa à qual pertence
        if (!SecurityUtils.isAdministrador()) {
            Long idEmpresaToken = SecurityUtils.getCurrentIdEmpresa();
            if (!id.equals(idEmpresaToken)) {
                throw new UnauthorizedException("Acesso negado à empresa solicitada");
            }
        }

        return empresaMapper.toResponse(empresa);
    }
}
