package com.scasistemas.msbluedot.application.usecases;

import com.scasistemas.msbluedot.domain.entities.AuditLog;
import com.scasistemas.msbluedot.domain.entities.Produto;
import com.scasistemas.msbluedot.domain.exceptions.ResourceNotFoundException;
import com.scasistemas.msbluedot.domain.exceptions.UnauthorizedException;
import com.scasistemas.msbluedot.domain.repositories.IAuditLogRepository;
import com.scasistemas.msbluedot.domain.repositories.IProdutoRepository;
import com.scasistemas.msbluedot.infrastructure.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use Case para exclusão (soft delete) de produto.
 * Verifica permissão de empresa. Imagens associadas não são deletadas
 * automaticamente.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeleteProdutoUseCase {

    private final IProdutoRepository produtoRepository;
    private final IAuditLogRepository auditLogRepository;

    @Transactional
    public void execute(String id) {
        log.info("[DeleteProduto] Soft delete produto id={}", id);

        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado: " + id));

        // Verificar permissão de empresa para não-admins
        if (!SecurityUtils.isAdministrador()) {
            Long idEmpresaToken = SecurityUtils.getCurrentIdEmpresa();
            if (produto.getIdEmpresa() != null && !produto.getIdEmpresa().equals(idEmpresaToken)) {
                throw new UnauthorizedException("Acesso negado ao produto solicitado");
            }
        }

        produtoRepository.deleteById(id);

        auditLogRepository.save(AuditLog.builder()
                .acao("DELETAR_PRODUTO")
                .usuario(SecurityUtils.getCurrentUsername())
                .idEmpresa(produto.getIdEmpresa())
                .detalhes("Produto desativado: " + produto.getId() + " | ean=" + produto.getCodigoEan())
                .build());

        log.info("[DeleteProduto] Produto id={} desativado com sucesso", id);
    }
}

