package com.scasistemas.msimagens.application.usecases;

import com.scasistemas.msimagens.TestSecurityHelper;
import com.scasistemas.msimagens.application.dto.usuario.UsuarioResponse;
import com.scasistemas.msimagens.application.mappers.UsuarioMapper;
import com.scasistemas.msimagens.domain.entities.Usuario;
import com.scasistemas.msimagens.domain.enums.RoleEnum;
import com.scasistemas.msimagens.domain.exceptions.ResourceNotFoundException;
import com.scasistemas.msimagens.domain.exceptions.UnauthorizedException;
import com.scasistemas.msimagens.domain.repositories.IUsuarioRepository;
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
@DisplayName("GetUsuarioUseCase - Testes unitários")
class GetUsuarioUseCaseTest {

    @Mock
    private IUsuarioRepository usuarioRepository;
    @Mock
    private UsuarioMapper usuarioMapper;

    @InjectMocks
    private GetUsuarioUseCase getUsuarioUseCase;

    @AfterEach
    void tearDown() {
        TestSecurityHelper.clearContext();
    }

    @Test
    @DisplayName("Deve retornar usuário quando ADMIN busca qualquer usuário")
    void deveRetornarUsuarioParaAdmin() {
        TestSecurityHelper.mockAdminContext();
        Usuario usuario = Usuario.builder().id(5L).nome("outro").idEmpresa(2L).role(RoleEnum.USUARIO).build();
        UsuarioResponse response = new UsuarioResponse();
        response.setId(5L);

        when(usuarioRepository.findById(5L)).thenReturn(Optional.of(usuario));
        when(usuarioMapper.toResponse(usuario)).thenReturn(response);

        UsuarioResponse result = getUsuarioUseCase.execute(5L);

        assertThat(result.getId()).isEqualTo(5L);
    }

    @Test
    @DisplayName("Deve retornar usuário quando USUARIO busca da mesma empresa")
    void deveRetornarUsuarioDaMesmaEmpresa() {
        TestSecurityHelper.mockUsuarioContext(); // idEmpresa=1
        Usuario usuario = Usuario.builder().id(3L).nome("colega").idEmpresa(1L).role(RoleEnum.USUARIO).build();
        UsuarioResponse response = new UsuarioResponse();
        response.setId(3L);

        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(usuario));
        when(usuarioMapper.toResponse(usuario)).thenReturn(response);

        UsuarioResponse result = getUsuarioUseCase.execute(3L);

        assertThat(result.getId()).isEqualTo(3L);
    }

    @Test
    @DisplayName("Deve lançar UnauthorizedException quando USUARIO busca usuário de outra empresa")
    void deveLancarExcecaoParaUsuarioDeOutraEmpresa() {
        TestSecurityHelper.mockUsuarioContext(); // idEmpresa=1
        Usuario usuario = Usuario.builder().id(5L).nome("outro").idEmpresa(99L).role(RoleEnum.USUARIO).build();

        when(usuarioRepository.findById(5L)).thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> getUsuarioUseCase.execute(5L))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException para usuário inexistente")
    void deveLancarExcecaoParaUsuarioInexistente() {
        TestSecurityHelper.mockAdminContext();
        when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> getUsuarioUseCase.execute(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
