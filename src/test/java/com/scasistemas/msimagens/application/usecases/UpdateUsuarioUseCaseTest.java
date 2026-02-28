package com.scasistemas.msimagens.application.usecases;

import com.scasistemas.msimagens.TestSecurityHelper;
import com.scasistemas.msimagens.application.dto.usuario.UsuarioRequest;
import com.scasistemas.msimagens.application.dto.usuario.UsuarioResponse;
import com.scasistemas.msimagens.application.mappers.UsuarioMapper;
import com.scasistemas.msimagens.domain.entities.AuditLog;
import com.scasistemas.msimagens.domain.entities.Usuario;
import com.scasistemas.msimagens.domain.enums.RoleEnum;
import com.scasistemas.msimagens.domain.exceptions.ResourceNotFoundException;
import com.scasistemas.msimagens.domain.exceptions.UnauthorizedException;
import com.scasistemas.msimagens.domain.repositories.IAuditLogRepository;
import com.scasistemas.msimagens.domain.repositories.IUsuarioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateUsuarioUseCase - Testes unitários")
class UpdateUsuarioUseCaseTest {

    @Mock
    private IUsuarioRepository usuarioRepository;
    @Mock
    private IAuditLogRepository auditLogRepository;
    @Mock
    private UsuarioMapper usuarioMapper;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UpdateUsuarioUseCase updateUsuarioUseCase;

    @AfterEach
    void tearDown() {
        TestSecurityHelper.clearContext();
    }

    @Test
    @DisplayName("ADMIN deve atualizar qualquer usuário incluindo role e idEmpresa")
    void adminDeveAtualizarQualquerUsuario() {
        TestSecurityHelper.mockAdminContext();
        Usuario usuario = Usuario.builder().id(5L).nome("user5").idEmpresa(1L).role(RoleEnum.USUARIO).build();

        UsuarioRequest request = new UsuarioRequest();
        request.setRole(RoleEnum.ADMINISTRADOR);
        request.setIdEmpresa(2L);

        UsuarioResponse response = new UsuarioResponse();
        response.setId(5L);

        when(usuarioRepository.findById(5L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(usuario)).thenReturn(usuario);
        when(usuarioMapper.toResponse(usuario)).thenReturn(response);
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(null);

        UsuarioResponse result = updateUsuarioUseCase.execute(5L, request);

        assertThat(result.getId()).isEqualTo(5L);
        assertThat(usuario.getRole()).isEqualTo(RoleEnum.ADMINISTRADOR);
        assertThat(usuario.getIdEmpresa()).isEqualTo(2L);
    }

    @Test
    @DisplayName("USUARIO só pode atualizar a si mesmo")
    void usuarioSoPodeAtualizarASiMesmo() {
        TestSecurityHelper.mockUsuarioContext(); // nome="usuario.teste"
        Usuario usuario = Usuario.builder().id(2L).nome("usuario.teste").idEmpresa(1L).role(RoleEnum.USUARIO).build();

        UsuarioRequest request = new UsuarioRequest();
        UsuarioResponse response = new UsuarioResponse();
        response.setId(2L);

        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(usuario)).thenReturn(usuario);
        when(usuarioMapper.toResponse(usuario)).thenReturn(response);
        when(auditLogRepository.save(any())).thenReturn(null);

        UsuarioResponse result = updateUsuarioUseCase.execute(2L, request);

        assertThat(result.getId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("Deve lançar UnauthorizedException quando USUARIO tenta atualizar outro usuário")
    void deveLancarExcecaoQuandoUsuarioAtualizaOutro() {
        TestSecurityHelper.mockUsuarioContext(); // nome="usuario.teste"
        Usuario usuario = Usuario.builder().id(5L).nome("outro.user").idEmpresa(1L).role(RoleEnum.USUARIO).build();

        when(usuarioRepository.findById(5L)).thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> updateUsuarioUseCase.execute(5L, new UsuarioRequest()))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    @DisplayName("Deve codificar senha quando fornecida")
    void deveCodificarSenhaQuandoFornecida() {
        TestSecurityHelper.mockAdminContext();
        Usuario usuario = Usuario.builder().id(1L).nome("admin").idEmpresa(1L).role(RoleEnum.ADMINISTRADOR).build();
        UsuarioRequest request = new UsuarioRequest();
        request.setSenha("novaSenha123");
        UsuarioResponse response = new UsuarioResponse();
        response.setId(1L);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.encode("novaSenha123")).thenReturn("$2a$12$encodedHash");
        when(usuarioRepository.save(usuario)).thenReturn(usuario);
        when(usuarioMapper.toResponse(usuario)).thenReturn(response);
        when(auditLogRepository.save(any())).thenReturn(null);

        updateUsuarioUseCase.execute(1L, request);

        assertThat(usuario.getSenha()).isEqualTo("$2a$12$encodedHash");
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException para usuário inexistente")
    void deveLancarExcecaoParaUsuarioInexistente() {
        TestSecurityHelper.mockAdminContext();
        when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> updateUsuarioUseCase.execute(999L, new UsuarioRequest()))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
