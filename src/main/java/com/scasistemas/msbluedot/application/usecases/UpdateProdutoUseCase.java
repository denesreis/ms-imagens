package com.scasistemas.msbluedot.application.usecases;

import com.scasistemas.msbluedot.application.dto.produto.ProdutoRequest;
import com.scasistemas.msbluedot.application.dto.produto.ProdutoResponse;
import com.scasistemas.msbluedot.application.mappers.ProdutoMapper;
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
 * Use Case para atualização de produto.
 * ADMIN pode atualizar qualquer produto; USUARIO apenas produtos da sua
 * empresa.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateProdutoUseCase {

    private final IProdutoRepository produtoRepository;
    private final IAuditLogRepository auditLogRepository;
    private final ProdutoMapper produtoMapper;

    @Transactional
    public ProdutoResponse execute(String id, ProdutoRequest request) {
        log.info("[UpdateProduto] Atualizando produto id={}", id);

        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado: " + id));

        // Verificar permissão de empresa
        verificarPermissao(produto);

        produto.setDescricao(request.getDescricao());
        produto.setDiscriminacao(request.getDiscriminacao());
        produto.setCodigoErp(request.getCodigoErp());
        produto.setCodigoEan(request.getCodigoEan());
        if (SecurityUtils.isAdministrador() && request.getIdEmpresa() != null) {
            produto.setIdEmpresa(request.getIdEmpresa());
        }

        Produto atualizado = produtoRepository.save(produto);

        auditLogRepository.save(AuditLog.builder()
                .acao("ATUALIZAR_PRODUTO")
                .usuario(SecurityUtils.getCurrentUsername())
                .idEmpresa(atualizado.getIdEmpresa())
                .detalhes("Produto atualizado: " + atualizado.getId())
                .build());

        log.info("[UpdateProduto] Produto id={} atualizado com sucesso", id);
        return produtoMapper.toResponse(atualizado);
    }

    private void verificarPermissao(Produto produto) {
        if (!SecurityUtils.isAdministrador()) {
            Long idEmpresaToken = SecurityUtils.getCurrentIdEmpresa();
            if (produto.getIdEmpresa() != null && !produto.getIdEmpresa().equals(idEmpresaToken)) {
                throw new UnauthorizedException("Acesso negado ao produto solicitado");
            }
        }
    }
}

