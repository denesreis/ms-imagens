package com.scasistemas.msimagens.application.usecases;

import com.scasistemas.msimagens.application.dto.auth.LoginRequest;
import com.scasistemas.msimagens.application.dto.auth.LoginResponse;
import com.scasistemas.msimagens.application.services.RefreshTokenService;
import com.scasistemas.msimagens.config.JwtProperties;
import com.scasistemas.msimagens.config.SecurityProperties;
import com.scasistemas.msimagens.domain.entities.AuditLog;
import com.scasistemas.msimagens.domain.entities.Usuario;
import com.scasistemas.msimagens.domain.enums.RoleEnum;
import com.scasistemas.msimagens.domain.exceptions.AccountLockedException;
import com.scasistemas.msimagens.domain.repositories.IAuditLogRepository;
import com.scasistemas.msimagens.domain.repositories.IUsuarioRepository;
import com.scasistemas.msimagens.infrastructure.security.JwtTokenProvider;
import com.scasistemas.msimagens.infrastructure.security.LoginAttemptService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthenticateUserUseCase - Testes unitários")
class AuthenticateUserUseCaseTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private JwtProperties jwtProperties;
    @Mock
    private RefreshTokenService refreshTokenService;
    @Mock
    private LoginAttemptService loginAttemptService;
    @Mock
    private SecurityProperties securityProperties;
    @Mock
    private IUsuarioRepository usuarioRepository;
    @Mock
    private IAuditLogRepository auditLogRepository;

    @InjectMocks
    private AuthenticateUserUseCase authenticateUserUseCase;

    private Usuario usuario;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        usuario = Usuario.builder()
                .id(1L)
                .nome("admin")
                .role(RoleEnum.ADMINISTRADOR)
                .idEmpresa(1L)
                .ativo(true)
                .tentativasLogin(0)
                .build();

        loginRequest = LoginRequest.builder()
                .nome("admin")
                .senha("senha123")
                .build();
    }

    @Test
    @DisplayName("Deve autenticar com sucesso e retornar tokens")
    void deveAutenticarComSucesso() {
        when(loginAttemptService.isBlocked("admin")).thenReturn(false);
        Authentication auth = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(auth);
        when(usuarioRepository.findByNome("admin")).thenReturn(Optional.of(usuario));
        when(jwtTokenProvider.generateAccessToken(usuario)).thenReturn("access-token-jwt");
        when(refreshTokenService.createRefreshToken(usuario)).thenReturn("refresh-token-jwt");
        when(jwtProperties.getExpiration()).thenReturn(1_800_000L);
        when(jwtProperties.getRefreshExpiration()).thenReturn(604_800_000L);
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(null);

        LoginResponse response = authenticateUserUseCase.execute(loginRequest, "127.0.0.1");

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("access-token-jwt");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token-jwt");
        assertThat(response.getExpiresIn()).isEqualTo(1800L);
        assertThat(response.getRefreshExpiresIn()).isEqualTo(604800L);
        assertThat(response.getUsuario().getNome()).isEqualTo("admin");
        assertThat(response.getUsuario().getRole()).isEqualTo(RoleEnum.ADMINISTRADOR);

        verify(loginAttemptService).loginSucceeded("admin");
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    @DisplayName("Deve lançar AccountLockedException quando login está bloqueado")
    void deveLancarExcecaoQuandoLoginBloqueado() {
        when(loginAttemptService.isBlocked("admin")).thenReturn(true);
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(null);

        assertThatThrownBy(() -> authenticateUserUseCase.execute(loginRequest, "127.0.0.1"))
                .isInstanceOf(AccountLockedException.class)
                .hasMessageContaining("bloqueada");

        verify(authenticationManager, never()).authenticate(any());
    }

    @Test
    @DisplayName("Deve incrementar tentativas e lançar BadCredentialsException para senha inválida")
    void deveIncrementarTentativasParaSenhaInvalida() {
        SecurityProperties.Login loginProps = new SecurityProperties.Login();
        loginProps.setMaxAttempts(5);
        loginProps.setBlockDuration(900_000L);

        when(loginAttemptService.isBlocked("admin")).thenReturn(false);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));
        when(usuarioRepository.findByNome("admin")).thenReturn(Optional.of(usuario));
        when(securityProperties.getLogin()).thenReturn(loginProps);
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(null);

        assertThatThrownBy(() -> authenticateUserUseCase.execute(loginRequest, "127.0.0.1"))
                .isInstanceOf(BadCredentialsException.class);

        verify(loginAttemptService).loginFailed("admin");
        verify(usuarioRepository).save(usuario);
    }

    @Test
    @DisplayName("Deve lançar AccountLockedException quando conta está bloqueada no banco")
    void deveLancarExcecaoQuandoContaBloqueadaNoBanco() {
        usuario.setBloqueadoAte(java.time.LocalDateTime.now().plusMinutes(10));

        when(loginAttemptService.isBlocked("admin")).thenReturn(false);
        Authentication auth = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(auth);
        when(usuarioRepository.findByNome("admin")).thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> authenticateUserUseCase.execute(loginRequest, "127.0.0.1"))
                .isInstanceOf(AccountLockedException.class);
    }

    @Test
    @DisplayName("Deve lançar BadCredentialsException quando usuário não encontrado após autenticação")
    void deveLancarExcecaoQuandoUsuarioNaoEncontrado() {
        when(loginAttemptService.isBlocked("admin")).thenReturn(false);
        Authentication auth = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(auth);
        when(usuarioRepository.findByNome("admin")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authenticateUserUseCase.execute(loginRequest, "127.0.0.1"))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    @DisplayName("Deve registrar audit log de LOGIN bem-sucedido")
    void deveRegistrarAuditLogDeLogin() {
        when(loginAttemptService.isBlocked("admin")).thenReturn(false);
        Authentication auth = mock(Authentication.class);
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(usuarioRepository.findByNome("admin")).thenReturn(Optional.of(usuario));
        when(jwtTokenProvider.generateAccessToken(usuario)).thenReturn("token");
        when(refreshTokenService.createRefreshToken(usuario)).thenReturn("refresh");
        when(jwtProperties.getExpiration()).thenReturn(1_800_000L);
        when(jwtProperties.getRefreshExpiration()).thenReturn(604_800_000L);
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(null);

        authenticateUserUseCase.execute(loginRequest, "192.168.1.1");

        verify(auditLogRepository).save(argThat(log -> "LOGIN".equals(log.getAcao()) &&
                "admin".equals(log.getUsuario()) &&
                "192.168.1.1".equals(log.getIp())));
    }

    @Test
    @DisplayName("Deve registrar audit log de LOGIN_BLOQUEADO")
    void deveRegistrarAuditLogDeLoginBloqueado() {
        when(loginAttemptService.isBlocked("admin")).thenReturn(true);
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(null);

        try {
            authenticateUserUseCase.execute(loginRequest, "10.0.0.1");
        } catch (AccountLockedException ignored) {
        }

        verify(auditLogRepository).save(argThat(log -> "LOGIN_BLOQUEADO".equals(log.getAcao())));
    }
}
