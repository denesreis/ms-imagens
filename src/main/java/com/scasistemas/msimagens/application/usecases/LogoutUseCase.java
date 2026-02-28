package com.scasistemas.msimagens.application.usecases;

import com.scasistemas.msimagens.application.services.RefreshTokenService;
import com.scasistemas.msimagens.domain.entities.AuditLog;
import com.scasistemas.msimagens.domain.repositories.IAuditLogRepository;
import com.scasistemas.msimagens.infrastructure.security.TokenBlacklistService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Use Case de logout: invalida o access token (blacklist) e revoga o refresh
 * token.
 *
 * <p>
 * Após o logout:
 * <ul>
 * <li>O access token é adicionado à blacklist em memória (Caffeine)</li>
 * <li>O refresh token é marcado como revogado no banco</li>
 * <li>O {@link SecurityContextHolder} é limpo</li>
 * <li>Um registro de auditoria é gravado</li>
 * </ul>
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LogoutUseCase {

    private final TokenBlacklistService tokenBlacklistService;
    private final RefreshTokenService refreshTokenService;
    private final IAuditLogRepository auditLogRepository;

    /**
     * Executa o logout do usuário autenticado.
     *
     * @param request      requisição HTTP (para extrair o Bearer token)
     * @param refreshToken refresh token a revogar (pode ser null)
     * @param username     nome do usuário que está fazendo logout
     * @param idEmpresa    ID da empresa do usuário
     * @param ip           endereço IP do cliente
     */
    @Transactional
    public void execute(HttpServletRequest request, String refreshToken,
            String username, Long idEmpresa, String ip) {
        // 1. Adicionar access token à blacklist
        String accessToken = extractToken(request);
        if (StringUtils.hasText(accessToken)) {
            tokenBlacklistService.blacklistToken(accessToken);
            log.debug("[Logout] Access token adicionado à blacklist para '{}'", username);
        }

        // 2. Revogar refresh token no banco
        if (StringUtils.hasText(refreshToken)) {
            refreshTokenService.revokeRefreshToken(refreshToken);
            log.debug("[Logout] Refresh token revogado para '{}'", username);
        }

        // 3. Limpar contexto de segurança
        SecurityContextHolder.clearContext();

        // 4. Audit log
        try {
            auditLogRepository.save(AuditLog.builder()
                    .acao("LOGOUT")
                    .usuario(username)
                    .idEmpresa(idEmpresa)
                    .ip(ip)
                    .detalhes("Logout realizado com sucesso")
                    .build());
        } catch (Exception e) {
            log.warn("[Audit] Falha ao gravar audit de logout: {}", e.getMessage());
        }

        log.info("[Logout] Logout realizado para '{}'", username);
    }

    // ─────────────────────────────────────────────────────────────────

    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}
