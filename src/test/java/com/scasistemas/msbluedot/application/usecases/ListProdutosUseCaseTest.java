package com.scasistemas.msbluedot.application.usecases;

import com.scasistemas.msbluedot.TestSecurityHelper;
import com.scasistemas.msbluedot.application.dto.produto.ProdutoResponse;
import com.scasistemas.msbluedot.application.mappers.ProdutoMapper;
import com.scasistemas.msbluedot.domain.entities.Produto;
import com.scasistemas.msbluedot.domain.repositories.IProdutoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ListProdutosUseCase - Testes unitários")
class ListProdutosUseCaseTest {

    @Mock
    private IProdutoRepository produtoRepository;
    @Mock
    private ProdutoMapper produtoMapper;

    @InjectMocks
    private ListProdutosUseCase listProdutosUseCase;

    @AfterEach
    void tearDown() {
        TestSecurityHelper.clearContext();
    }

    @Test
    @DisplayName("ADMIN deve ver todos os produtos")
    void adminDeveVerTodosProdutos() {
        TestSecurityHelper.mockAdminContext();
        Produto p1 = Produto.builder().id("p1").idEmpresa(1L).build();
        Produto p2 = Produto.builder().id("p2").idEmpresa(2L).build();
        ProdutoResponse r1 = new ProdutoResponse();
        r1.setId("p1");
        ProdutoResponse r2 = new ProdutoResponse();
        r2.setId("p2");

        when(produtoRepository.findAll()).thenReturn(List.of(p1, p2));
        when(produtoMapper.toResponse(p1)).thenReturn(r1);
        when(produtoMapper.toResponse(p2)).thenReturn(r2);

        Page<ProdutoResponse> result = listProdutosUseCase.execute(PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    @DisplayName("USUARIO deve ver apenas produtos da sua empresa")
    void usuarioDeveVerApenasDaSuaEmpresa() {
        TestSecurityHelper.mockUsuarioContext(); // idEmpresa=1
        Produto p1 = Produto.builder().id("p1").idEmpresa(1L).build();
        ProdutoResponse r1 = new ProdutoResponse();
        r1.setId("p1");

        when(produtoRepository.findByIdEmpresa(1L)).thenReturn(List.of(p1));
        when(produtoMapper.toResponse(p1)).thenReturn(r1);

        Page<ProdutoResponse> result = listProdutosUseCase.execute(PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
    }
}

