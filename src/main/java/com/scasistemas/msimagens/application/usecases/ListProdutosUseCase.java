package com.scasistemas.msimagens.application.usecases;

import com.scasistemas.msimagens.application.dto.produto.ProdutoResponse;
import com.scasistemas.msimagens.application.mappers.ProdutoMapper;
import com.scasistemas.msimagens.domain.entities.Produto;
import com.scasistemas.msimagens.domain.repositories.IProdutoRepository;
import com.scasistemas.msimagens.infrastructure.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Use Case para listagem de produtos com paginação.
 * ADMIN vê todos; USUARIO vê apenas produtos da sua empresa.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ListProdutosUseCase {

    private final IProdutoRepository produtoRepository;
    private final ProdutoMapper produtoMapper;

    @Transactional(readOnly = true)
    public Page<ProdutoResponse> execute(Pageable pageable) {
        log.debug("[ListProdutos] Listando produtos page={} size={}", pageable.getPageNumber(), pageable.getPageSize());

        List<Produto> todos;
        if (SecurityUtils.isAdministrador()) {
            todos = produtoRepository.findAll();
        } else {
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
