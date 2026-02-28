package com.scasistemas.msimagens.application.usecases;

import com.scasistemas.msimagens.TestSecurityHelper;
import com.scasistemas.msimagens.application.dto.usuario.UsuarioResponse;
import com.scasistemas.msimagens.application.mappers.UsuarioMapper;
import com.scasistemas.msimagens.domain.entities.Usuario;
import com.scasistemas.msimagens.domain.enums.RoleEnum;
import com.scasistemas.msimagens.domain.repositories.IUsuarioRepository;
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
@DisplayName("ListUsuariosUseCase - Testes unitários")
class ListUsuariosUseCaseTest {

    @Mock
    private IUsuarioRepository usuarioRepository;
    @Mock
    private UsuarioMapper usuarioMapper;

    @InjectMocks
    private ListUsuariosUseCase listUsuariosUseCase;

    @AfterEach
    void tearDown() {
        TestSecurityHelper.clearContext();
    }

    @Test
    @DisplayName("ADMIN deve ver todos os usuários")
    void adminDeveVerTodosUsuarios() {
        TestSecurityHelper.mockAdminContext();
        Usuario u1 = Usuario.builder().id(1L).nome("u1").idEmpresa(1L).role(RoleEnum.USUARIO).build();
        Usuario u2 = Usuario.builder().id(2L).nome("u2").idEmpresa(2L).role(RoleEnum.USUARIO).build();
        UsuarioResponse r1 = new UsuarioResponse();
        r1.setId(1L);
        UsuarioResponse r2 = new UsuarioResponse();
        r2.setId(2L);

        when(usuarioRepository.findAll()).thenReturn(List.of(u1, u2));
        when(usuarioMapper.toResponse(u1)).thenReturn(r1);
        when(usuarioMapper.toResponse(u2)).thenReturn(r2);

        Page<UsuarioResponse> result = listUsuariosUseCase.execute(PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    @DisplayName("USUARIO deve ver apenas usuários da mesma empresa")
    void usuarioDeveVerApenasDaMesmaEmpresa() {
        TestSecurityHelper.mockUsuarioContext(); // idEmpresa=1
        Usuario u1 = Usuario.builder().id(1L).nome("u1").idEmpresa(1L).role(RoleEnum.USUARIO).build();
        UsuarioResponse r1 = new UsuarioResponse();
        r1.setId(1L);

        when(usuarioRepository.findByIdEmpresa(1L)).thenReturn(List.of(u1));
        when(usuarioMapper.toResponse(u1)).thenReturn(r1);

        Page<UsuarioResponse> result = listUsuariosUseCase.execute(PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
    }
}
