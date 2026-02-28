package com.scasistemas.msimagens.application.usecases;

import com.scasistemas.msimagens.TestSecurityHelper;
import com.scasistemas.msimagens.application.dto.imagem.ImagemResponse;
import com.scasistemas.msimagens.application.mappers.ImagemMapper;
import com.scasistemas.msimagens.domain.entities.Imagem;
import com.scasistemas.msimagens.domain.enums.TipoArmazenamentoEnum;
import com.scasistemas.msimagens.domain.exceptions.ResourceNotFoundException;
import com.scasistemas.msimagens.domain.exceptions.UnauthorizedException;
import com.scasistemas.msimagens.domain.repositories.IImagemRepository;
import com.scasistemas.msimagens.domain.entities.Produto;
import com.scasistemas.msimagens.domain.repositories.IProdutoRepository;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetImagensByProdutoUseCase - Testes unitários")
class GetImagensByProdutoUseCaseTest {

    @Mock
    private IImagemRepository imagemRepository;
    @Mock
    private IProdutoRepository produtoRepository;
    @Mock
    private ImagemMapper imagemMapper;

    @InjectMocks
    private GetImagensByProdutoUseCase getImagensByProdutoUseCase;

    @AfterEach
    void tearDown() {
        TestSecurityHelper.clearContext();
    }

    @Test
    @DisplayName("Deve listar imagens do produto para ADMIN")
    void deveListarImagensDoProdutoParaAdmin() {
        TestSecurityHelper.mockAdminContext();
        Produto produto = Produto.builder().id("prod-001").idEmpresa(1L).build();
        Imagem img = Imagem.builder().id("img-001").idProduto("prod-001").build();
        ImagemResponse response = new ImagemResponse();
        response.setId("img-001");

        when(produtoRepository.findById("prod-001")).thenReturn(Optional.of(produto));
        when(imagemRepository.findByIdProduto("prod-001")).thenReturn(List.of(img));
        when(imagemMapper.toResponse(img)).thenReturn(response);

        Page<ImagemResponse> result = getImagensByProdutoUseCase.execute("prod-001", PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("Deve lançar UnauthorizedException quando USUARIO acessa produto de outra empresa")
    void deveLancarExcecaoParaUsuarioDeOutraEmpresa() {
        TestSecurityHelper.mockUsuarioContext(); // idEmpresa=1
        Produto produto = Produto.builder().id("prod-001").idEmpresa(99L).build();

        when(produtoRepository.findById("prod-001")).thenReturn(Optional.of(produto));

        assertThatThrownBy(() -> getImagensByProdutoUseCase.execute("prod-001", PageRequest.of(0, 10)))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException para produto inexistente")
    void deveLancarExcecaoParaProdutoInexistente() {
        TestSecurityHelper.mockAdminContext();
        when(produtoRepository.findById("nao-existe")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> getImagensByProdutoUseCase.execute("nao-existe", PageRequest.of(0, 10)))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
