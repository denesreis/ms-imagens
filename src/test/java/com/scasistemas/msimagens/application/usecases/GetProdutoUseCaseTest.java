package com.scasistemas.msimagens.application.usecases;

import com.scasistemas.msimagens.TestSecurityHelper;
import com.scasistemas.msimagens.application.dto.produto.ProdutoResponse;
import com.scasistemas.msimagens.application.mappers.ProdutoMapper;
import com.scasistemas.msimagens.domain.entities.Produto;
import com.scasistemas.msimagens.domain.exceptions.ResourceNotFoundException;
import com.scasistemas.msimagens.domain.exceptions.UnauthorizedException;
import com.scasistemas.msimagens.domain.repositories.IProdutoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetProdutoUseCase - Testes unitários")
class GetProdutoUseCaseTest {

    @Mock
    private IProdutoRepository produtoRepository;
    @Mock
    private ProdutoMapper produtoMapper;

    @InjectMocks
    private GetProdutoUseCase getProdutoUseCase;

    @AfterEach
    void tearDown() {
        TestSecurityHelper.clearContext();
    }

    @Test
    @DisplayName("Deve retornar produto quando ADMIN busca qualquer produto")
    void deveRetornarProdutoParaAdmin() {
        TestSecurityHelper.mockAdminContext();
        Produto produto = Produto.builder().id("prod-001").idEmpresa(2L).build();
        ProdutoResponse response = new ProdutoResponse();
        response.setId("prod-001");

        when(produtoRepository.findById("prod-001")).thenReturn(Optional.of(produto));
        when(produtoMapper.toResponse(produto)).thenReturn(response);

        ProdutoResponse result = getProdutoUseCase.execute("prod-001");

        assertThat(result.getId()).isEqualTo("prod-001");
    }

    @Test
    @DisplayName("Deve lançar UnauthorizedException quando USUARIO busca produto de outra empresa")
    void deveLancarExcecaoParaUsuarioDeOutraEmpresa() {
        TestSecurityHelper.mockUsuarioContext(); // idEmpresa=1
        Produto produto = Produto.builder().id("prod-001").idEmpresa(99L).build();

        when(produtoRepository.findById("prod-001")).thenReturn(Optional.of(produto));

        assertThatThrownBy(() -> getProdutoUseCase.execute("prod-001"))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException para produto inexistente")
    void deveLancarExcecaoParaProdutoInexistente() {
        TestSecurityHelper.mockAdminContext();
        when(produtoRepository.findById("nao-existe")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> getProdutoUseCase.execute("nao-existe"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
