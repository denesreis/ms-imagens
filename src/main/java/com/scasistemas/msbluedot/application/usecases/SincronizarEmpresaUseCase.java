package com.scasistemas.msbluedot.application.usecases;

import com.scasistemas.msbluedot.application.dto.empresa.EmpresaResponse;
import com.scasistemas.msbluedot.application.dto.sync.SincronizarEmpresaRequest;
import com.scasistemas.msbluedot.application.mappers.EmpresaMapper;
import com.scasistemas.msbluedot.domain.entities.Empresa;
import com.scasistemas.msbluedot.domain.repositories.IEmpresaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use Case para sincronização (upsert) de empresa vinda do novo-mercador.
 *
 * <p>
 * Regras:
 * <ul>
 * <li>Se a empresa com o {@code codigoErp} informado não existir → cria</li>
 * <li>Se já existir → atualiza o nome</li>
 * <li>A proteção de acesso é feita pelo {@code SyncApiKeyFilter}, não
 * por @PreAuthorize</li>
 * </ul>
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SincronizarEmpresaUseCase {

    private final IEmpresaRepository empresaRepository;
    private final EmpresaMapper empresaMapper;

    @Transactional
    public EmpresaResponse execute(SincronizarEmpresaRequest request) {
        log.info("[SincronizarEmpresa] Upsert de empresa codigoErp='{}'", request.getCodigoErp());

        Empresa empresa = empresaRepository.findByCodigoErp(request.getCodigoErp())
                .map(existente -> {
                    log.info("[SincronizarEmpresa] Empresa existente id={} — atualizando nome", existente.getId());
                    existente.setNome(request.getNome());
                    return existente;
                })
                .orElseGet(() -> {
                    log.info("[SincronizarEmpresa] Empresa não encontrada — criando nova");
                    return Empresa.builder()
                            .codigoErp(request.getCodigoErp())
                            .nome(request.getNome())
                            .ativo(true)
                            .build();
                });

        Empresa salva = empresaRepository.save(empresa);
        log.info("[SincronizarEmpresa] Empresa id={} sincronizada com sucesso", salva.getId());
        return empresaMapper.toResponse(salva);
    }
}
