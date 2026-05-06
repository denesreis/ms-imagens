package com.scasistemas.msbluedot.application.usecases;

import com.scasistemas.msbluedot.application.dto.empresa.EmpresaResponse;
import com.scasistemas.msbluedot.application.mappers.EmpresaMapper;
import com.scasistemas.msbluedot.domain.entities.Empresa;
import com.scasistemas.msbluedot.domain.repositories.IEmpresaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Use Case para listagem de empresas (apenas ADMINISTRADOR).
 * Suporta paginação via {@link Pageable}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ListEmpresasUseCase {

    private final IEmpresaRepository empresaRepository;
    private final EmpresaMapper empresaMapper;

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Transactional(readOnly = true)
    public Page<EmpresaResponse> execute(Pageable pageable) {
        log.debug("[ListEmpresas] Listando empresas page={} size={}", pageable.getPageNumber(), pageable.getPageSize());

        List<Empresa> todas = empresaRepository.findAll();
        List<EmpresaResponse> responses = todas.stream()
                .map(empresaMapper::toResponse)
                .toList();

        // Aplicar paginação manualmente
        int total = responses.size();
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), total);

        List<EmpresaResponse> paginado = (start >= total) ? List.of() : responses.subList(start, end);
        return new PageImpl<>(paginado, pageable, total);
    }
}

