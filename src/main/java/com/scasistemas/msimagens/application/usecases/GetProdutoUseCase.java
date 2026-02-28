package com.scasistemas.msimagens.application.usecases;

import com.scasistemas.msimagens.application.dto.produto.ProdutoResponse;
import com.scasistemas.msimagens.application.mappers.ProdutoMapper;
import com.scasistemas.msimagens.domain.entities.Produto;
import com.scasistemas.msimagens.domain.exceptions.ResourceNotFoundException;
import com.scasistemas.msimagens.domain.exceptions.UnauthorizedException;
import com.scasistemas.msimagens.domain.repositories.IProdutoRepository;
import com.scasistemas.msimagens.infrastructure.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use Case para busca de produto por ID.
 * Verifica permissão de empresa para usuários não-admin.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GetProdutoUseCase {

    private final IProdutoRepository produtoRepository;
    private final ProdutoMapper produtoMapper;

    @Transactional(readOnly = true)
    public ProdutoResponse execute(String id) {
        log.debug("[GetProduto] Buscando produto id={}", id);

        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado: " + id));

        // Verificar permissão de empresa
        if (!SecurityUtils.isAdministrador()) {
            Long idEmpresaToken = SecurityUtils.getCurrentIdEmpresa();
            if (produto.getIdEmpresa() != null && !produto.getIdEmpresa().equals(idEmpresaToken)) {
                throw new UnauthorizedException("Acesso negado ao produto solicitado");
            }
        }

        return produtoMapper.toResponse(produto);
    }
}
