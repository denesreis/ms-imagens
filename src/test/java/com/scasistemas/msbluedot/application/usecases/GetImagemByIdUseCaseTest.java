package com.scasistemas.msbluedot.application.usecases;

import com.scasistemas.msbluedot.TestSecurityHelper;
import com.scasistemas.msbluedot.application.dto.imagem.ImagemResponse;
import com.scasistemas.msbluedot.application.mappers.ImagemMapper;
import com.scasistemas.msbluedot.domain.entities.Imagem;
import com.scasistemas.msbluedot.domain.enums.StatusImagemEnum;
import com.scasistemas.msbluedot.domain.enums.TipoArmazenamentoEnum;
import com.scasistemas.msbluedot.domain.exceptions.ResourceNotFoundException;
import com.scasistemas.msbluedot.domain.exceptions.UnauthorizedException;
import com.scasistemas.msbluedot.domain.repositories.IImagemRepository;
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
@DisplayName("GetImagemByIdUseCase - Testes unitários")
class GetImagemByIdUseCaseTest {

    @Mock
    private IImagemRepository imagemRepository;
    @Mock
    private ImagemMapper imagemMapper;

    @InjectMocks
    private GetImagemByIdUseCase getImagemByIdUseCase;

    @AfterEach
    void tearDown() {
        TestSecurityHelper.clearContext();
    }

    @Test
    @DisplayName("ADMIN deve acessar qualquer imagem")
    void adminDeveAcessarQualquerImagem() {
        TestSecurityHelper.mockAdminContext();
        Imagem imagem = Imagem.builder().id("img-001").idEmpresa(2L)
                .status(StatusImagemEnum.ATIVO).tipoArmazenamento(TipoArmazenamentoEnum.ABERTO).build();
        ImagemResponse response = new ImagemResponse();
        response.setId("img-001");

        when(imagemRepository.findById("img-001")).thenReturn(Optional.of(imagem));
        when(imagemMapper.toResponse(imagem)).thenReturn(response);

        ImagemResponse result = getImagemByIdUseCase.execute("img-001");

        assertThat(result.getId()).isEqualTo("img-001");
    }

    @Test
    @DisplayName("Deve lançar UnauthorizedException quando USUARIO acessa imagem de outra empresa")
    void deveLancarExcecaoParaUsuarioDeOutraEmpresa() {
        TestSecurityHelper.mockUsuarioContext(); // idEmpresa=1
        Imagem imagem = Imagem.builder().id("img-001").idEmpresa(99L).build();

        when(imagemRepository.findById("img-001")).thenReturn(Optional.of(imagem));

        assertThatThrownBy(() -> getImagemByIdUseCase.execute("img-001"))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException para imagem inexistente")
    void deveLancarExcecaoParaImagemInexistente() {
        TestSecurityHelper.mockAdminContext();
        when(imagemRepository.findById("nao-existe")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> getImagemByIdUseCase.execute("nao-existe"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}

