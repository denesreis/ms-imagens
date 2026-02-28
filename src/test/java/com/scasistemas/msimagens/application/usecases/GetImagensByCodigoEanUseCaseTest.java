package com.scasistemas.msimagens.application.usecases;

import com.scasistemas.msimagens.application.dto.imagem.ImagemProdutoResponse;
import com.scasistemas.msimagens.application.mappers.ImagemMapper;
import com.scasistemas.msimagens.domain.entities.Imagem;
import com.scasistemas.msimagens.domain.enums.StatusImagemEnum;
import com.scasistemas.msimagens.domain.enums.TipoArmazenamentoEnum;
import com.scasistemas.msimagens.domain.repositories.IImagemRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetImagensByCodigoEanUseCase - Testes unitários")
class GetImagensByCodigoEanUseCaseTest {

    @Mock
    private IImagemRepository imagemRepository;
    @Mock
    private ImagemMapper imagemMapper;

    @InjectMocks
    private GetImagensByCodigoEanUseCase getImagensByCodigoEanUseCase;

    @Test
    @DisplayName("Deve retornar imagens públicas para o EAN informado")
    void deveRetornarImagensPublicas() {
        Imagem imagem1 = Imagem.builder()
                .id("img-001")
                .idProduto("prod-001")
                .tipoArmazenamento(TipoArmazenamentoEnum.ABERTO)
                .status(StatusImagemEnum.ATIVO)
                .url("https://cf.url/img1/public")
                .filename("foto1.jpg")
                .build();

        ImagemProdutoResponse resp = new ImagemProdutoResponse();
        resp.setUrl("https://cf.url/img1/public");

        when(imagemRepository.findByCodigoEanAndTipoAberto("7891234567890"))
                .thenReturn(List.of(imagem1));
        when(imagemMapper.toProdutoResponse(imagem1)).thenReturn(resp);

        List<ImagemProdutoResponse> result = getImagensByCodigoEanUseCase.execute("7891234567890");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUrl()).isEqualTo("https://cf.url/img1/public");
        verify(imagemRepository).findByCodigoEanAndTipoAberto("7891234567890");
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há imagens para o EAN")
    void deveRetornarListaVaziaQuandoNaoHaImagens() {
        when(imagemRepository.findByCodigoEanAndTipoAberto("0000000000000"))
                .thenReturn(List.of());

        List<ImagemProdutoResponse> result = getImagensByCodigoEanUseCase.execute("0000000000000");

        assertThat(result).isEmpty();
    }
}
