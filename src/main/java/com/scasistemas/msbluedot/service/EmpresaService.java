package com.scasistemas.msbluedot.service;

import com.scasistemas.msbluedot.dto.EmpresaRequest;
import com.scasistemas.msbluedot.dto.EmpresaResponse;
import com.scasistemas.msbluedot.entity.AuditLog;
import com.scasistemas.msbluedot.entity.Empresa;
import com.scasistemas.msbluedot.exception.BusinessException;
import com.scasistemas.msbluedot.exception.ResourceNotFoundException;
import com.scasistemas.msbluedot.exception.UnauthorizedException;
import com.scasistemas.msbluedot.repository.AuditLogRepository;
import com.scasistemas.msbluedot.repository.EmpresaRepository;
import com.scasistemas.msbluedot.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmpresaService {

    private final EmpresaRepository empresaRepository;
    private final AuditLogRepository auditLogRepository;

    @Transactional
    public EmpresaResponse criar(EmpresaRequest request) {
        log.info("[CreateEmpresa] Criando empresa codigoErp='{}'", request.getCodigoErp());

        empresaRepository.findByCodigoErp(request.getCodigoErp()).ifPresent(e -> {
            throw new BusinessException("Já existe uma empresa com o código ERP: " + request.getCodigoErp());
        });

        Empresa empresa = Empresa.builder()
                .codigoErp(request.getCodigoErp())
                .nome(request.getNome())
                .ativo(true)
                .build();

        Empresa salva = empresaRepository.save(empresa);

        auditLogRepository.save(AuditLog.builder()
                .acao("CRIAR_EMPRESA")
                .usuario(SecurityUtils.getCurrentUsername())
                .idEmpresa(salva.getId())
                .detalhes("Empresa criada: " + salva.getNome())
                .build());

        log.info("[CreateEmpresa] Empresa id={} criada com sucesso", salva.getId());
        return toResponse(salva);
    }

    @Transactional
    public EmpresaResponse atualizar(Long id, EmpresaRequest request) {
        log.info("[UpdateEmpresa] Atualizando empresa id={}", id);

        Empresa empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada: " + id));

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
        return toResponse(atualizada);
    }

    @Transactional(readOnly = true)
    public EmpresaResponse buscarPorId(Long id) {
        log.debug("[GetEmpresa] Buscando empresa id={}", id);

        Empresa empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada: " + id));

        if (!SecurityUtils.isAdministrador()) {
            Long idEmpresaToken = SecurityUtils.getCurrentIdEmpresa();
            if (!id.equals(idEmpresaToken)) {
                throw new UnauthorizedException("Acesso negado à empresa solicitada");
            }
        }

        return toResponse(empresa);
    }

    @Transactional(readOnly = true)
    public Page<EmpresaResponse> listar(Pageable pageable) {
        log.debug("[ListEmpresas] Listando empresas page={} size={}", pageable.getPageNumber(), pageable.getPageSize());

        List<Empresa> todas = empresaRepository.findAll();
        List<EmpresaResponse> responses = todas.stream()
                .map(this::toResponse)
                .toList();

        int total = responses.size();
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), total);

        List<EmpresaResponse> paginado = (start >= total) ? List.of() : responses.subList(start, end);
        return new PageImpl<>(paginado, pageable, total);
    }

    @Transactional
    public void deletar(Long id) {
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

    public EmpresaResponse toResponse(Empresa e) {
        return EmpresaResponse.builder()
                .id(e.getId())
                .codigoErp(e.getCodigoErp())
                .nome(e.getNome())
                .ativo(e.getAtivo())
                .dataCriacao(e.getDataCriacao())
                .dataAtualizacao(e.getDataAtualizacao())
                .build();
    }
}
