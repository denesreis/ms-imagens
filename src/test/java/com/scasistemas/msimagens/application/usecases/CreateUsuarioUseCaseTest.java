package com.scasistemas.msimagens.application.usecases;

import com.scasistemas.msimagens.TestSecurityHelper;
import com.scasistemas.msimagens.application.dto.usuario.UsuarioRequest;
import com.scasistemas.msimagens.application.dto.usuario.UsuarioResponse;
import com.scasistemas.msimagens.application.mappers.UsuarioMapper;
import com.scasistemas.msimagens.domain.entities.AuditLog;
import com.scasistemas.msimagens.domain.entities.Usuario;
import com.scasistemas.msimagens.domain.enums.RoleEnum;
import com.scasistemas.msimagens.domain.exceptions.DuplicateUsernameException;
import com.scasistemas.msimagens.domain.exceptions.ResourceNotFoundException;
import com.scasistemas.msimagens.domain.repositories.IAuditLogRepository;
import com.scasistemas.msimagens.domain.repositories.IEmpresaRepository;
import com.scasistemas.msimagens.domain.repositories.IUsuarioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
@DisplayName("CreateUsuarioUseCase - Testes unitários")
class CreateUsuarioUseCaseTest {

    @Mock
    private IUsuarioRepository usuarioRepository;
    @Mock
    private IEmpresaRepository empresaRepository;
    @Mock
    private IAuditLogRepository auditLogRepository;
    @Mock
    private UsuarioMapper usuarioMapper;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private CreateUsuarioUseCase createUsuarioUseCase;

    @BeforeEach
    void setUp() {
        TestSecurityHelper.mockAdminContext();
    }

    @AfterEach
    void tearDown() {
        TestSecurityHelper.clearContext();
    }

    private UsuarioRequest buildRequest() {
        UsuarioRequest req = new UsuarioRequest();
        req.setNome("novo.usuario");
        req.setSenha("senha123456");
        req.setRole(RoleEnum.USUARIO);
        req.setIdEmpresa(1L);
        return req;
    }

    @Test
    @DisplayName("Deve criar usuário com sucesso")
    void deveCriarUsuarioComSucesso() {
        UsuarioRequest request = buildRequest();

        Usuario usuario = Usuario.builder()
                .nome("novo.usuario")
                .role(RoleEnum.USUARIO)
                .idEmpresa(1L)
                .build();

        Usuario salvo = Usuario.builder()
                .id(5L)
                .nome("novo.usuario")
                .role(RoleEnum.USUARIO)
                .idEmpresa(1L)
                .build();

        UsuarioResponse response = new UsuarioResponse();
        response.setId(5L);
        response.setNome("novo.usuario");

        when(usuarioRepository.findByNome("novo.usuario")).thenReturn(Optional.empty());
        when(empresaRepository.existsById(1L)).thenReturn(true);
        when(usuarioMapper.fromRequest(request)).thenReturn(usuario);
        when(passwordEncoder.encode("senha123456")).thenReturn("$2a$12$hashed");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(salvo);
        when(usuarioMapper.toResponse(salvo)).thenReturn(response);
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(null);

        UsuarioResponse result = createUsuarioUseCase.execute(request);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(5L);
        verify(passwordEncoder).encode("senha123456");
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Deve lançar DuplicateUsernameException para nome duplicado → HTTP 409")
    void deveLancarDuplicateUsernameExceptionParaNomeDuplicado() {
        UsuarioRequest request = buildRequest();

        Usuario existente = Usuario.builder().id(1L).nome("novo.usuario").build();
        when(usuarioRepository.findByNome("novo.usuario")).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> createUsuarioUseCase.execute(request))
                .isInstanceOf(DuplicateUsernameException.class)
                .hasMessageContaining("novo.usuario");

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException para empresa inexistente")
    void deveLancarExcecaoParaEmpresaInexistente() {
        UsuarioRequest request = buildRequest();

        when(usuarioRepository.findByNome("novo.usuario")).thenReturn(Optional.empty());
        when(empresaRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> createUsuarioUseCase.execute(request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve codificar a senha com BCrypt")
    void deveCodificarSenhaComBcrypt() {
        UsuarioRequest request = buildRequest();

        when(usuarioRepository.findByNome(anyString())).thenReturn(Optional.empty());
        when(empresaRepository.existsById(1L)).thenReturn(true);
        when(usuarioMapper.fromRequest(any())).thenReturn(Usuario.builder().nome("n").build());
        when(passwordEncoder.encode("senha123456")).thenReturn("$2a$12$bcrypt_hash");
        when(usuarioRepository.save(any())).thenReturn(Usuario.builder().id(1L).nome("n").build());
        when(usuarioMapper.toResponse(any())).thenReturn(new UsuarioResponse());
        when(auditLogRepository.save(any())).thenReturn(null);

        createUsuarioUseCase.execute(request);

        verify(passwordEncoder).encode("senha123456");
    }
}
