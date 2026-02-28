package com.scasistemas.msimagens.application.usecases;

import com.scasistemas.msimagens.application.services.RefreshTokenService;
import com.scasistemas.msimagens.domain.entities.AuditLog;
import com.scasistemas.msimagens.domain.repositories.IAuditLogRepository;
import com.scasistemas.msimagens.infrastructure.security.TokenBlacklistService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LogoutUseCase - Testes unitários")
class LogoutUseCaseTest {

    @Mock
    private TokenBlacklistService tokenBlacklistService;
    @Mock
    private RefreshTokenService refreshTokenService;
    @Mock
    private IAuditLogRepository auditLogRepository;
    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private LogoutUseCase logoutUseCase;

    @Test
    @DisplayName("Deve adicionar access token à blacklist durante logout")
    void deveAdicionarAccessTokenABlacklist() {
        when(request.getHeader(HttpHeaders.AUTHORIZATION))
                .thenReturn("Bearer eyJhbGciOiJIUzI1NiJ9.access.token");
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(null);

        logoutUseCase.execute(request, null, "usuario.teste", 1L, "127.0.0.1");

        verify(tokenBlacklistService).blacklistToken("eyJhbGciOiJIUzI1NiJ9.access.token");
    }

    @Test
    @DisplayName("Deve revogar refresh token quando fornecido")
    void deveRevogarRefreshTokenQuandoFornecido() {
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(null);
        when(auditLogRepository.save(any())).thenReturn(null);

        logoutUseCase.execute(request, "plain-refresh-token", "usuario.teste", 1L, "127.0.0.1");

        verify(refreshTokenService).revokeRefreshToken("plain-refresh-token");
    }

    @Test
    @DisplayName("Não deve tentar revogar refresh token quando não fornecido")
    void naoDeveRevogarRefreshTokenQuandoNaoFornecido() {
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(null);
        when(auditLogRepository.save(any())).thenReturn(null);

        logoutUseCase.execute(request, null, "usuario.teste", 1L, "127.0.0.1");

        verify(refreshTokenService, never()).revokeRefreshToken(anyString());
    }

    @Test
    @DisplayName("Deve registrar audit log de LOGOUT")
    void deveRegistrarAuditLogDeLogout() {
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(null);

        logoutUseCase.execute(request, null, "usuario.teste", 1L, "192.168.1.1");

        verify(auditLogRepository)
                .save(argThat(log -> "LOGOUT".equals(log.getAcao()) && "usuario.teste".equals(log.getUsuario())));
    }

    @Test
    @DisplayName("Deve ignorar ausência de Authorization header graciosamente")
    void deveIgnorarAusenciaDeHeader() {
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(null);
        when(auditLogRepository.save(any())).thenReturn(null);

        // Não deve lançar exceção
        logoutUseCase.execute(request, null, "usuario", 1L, "127.0.0.1");

        verify(tokenBlacklistService, never()).blacklistToken(anyString());
    }
}
