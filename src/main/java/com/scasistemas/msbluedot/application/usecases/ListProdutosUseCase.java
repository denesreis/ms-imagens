package com.scasistemas.msbluedot.application.usecases;

import com.scasistemas.msbluedot.application.dto.produto.ProdutoResponse;
import com.scasistemas.msbluedot.application.mappers.ProdutoMapper;
import com.scasistemas.msbluedot.domain.entities.Produto;
import com.scasistemas.msbluedot.domain.enums.TipoArmazenamentoEnum;
import com.scasistemas.msbluedot.domain.repositories.IImagemRepository;
import com.scasistemas.msbluedot.domain.repositories.IProdutoRepository;
import com.scasistemas.msbluedot.infrastructure.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Use Case para listagem de produtos com paginação.
 * ADMINISTRADOR (ms-bluedot) = MASTER (novo-mercador): exibe apenas produtos
 * com imagens ABERTO.
 * USUARIO (ms-bluedot) = ADMINISTRADOR (novo-mercador): exibe apenas produtos
 * da própria empresa.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ListProdutosUseCase {

    private final IProdutoRepository produtoRepository;
    private final IImagemRepository imagemRepository;
    private final ProdutoMapper produtoMapper;

    @Transactional(readOnly = true)
    public Page<ProdutoResponse> execute(Pageable pageable) {
        log.debug("[ListProdutos] Listando produtos page={} size={}", pageable.getPageNumber(), pageable.getPageSize());

        List<Produto> todos;
        if (SecurityUtils.isAdministrador()) {
            // MASTER (novo-mercador): exibe apenas produtos que possuem imagens ABERTO
            List<String> ids = imagemRepository.findDistinctIdProdutosByTipoArmazenamento(TipoArmazenamentoEnum.ABERTO);
            todos = ids.stream()
                    .map(produtoRepository::findById)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .toList();
        } else {
            // ADMINISTRADOR (novo-mercador): exibe apenas produtos da própria empresa
            Long idEmpresa = SecurityUtils.getCurrentIdEmpresa();
            todos = produtoRepository.findByIdEmpresa(idEmpresa);
        }

        List<ProdutoResponse> responses = todos.stream()
                .map(produtoMapper::toResponse)
                .toList();

        int total = responses.size();
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), total);

        List<ProdutoResponse> paginado = (start >= total) ? List.of() : responses.subList(start, end);
        return new PageImpl<>(paginado, pageable, total);
    }

}
