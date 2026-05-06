package com.scasistemas.msbluedot.application.usecases;

import com.scasistemas.msbluedot.application.dto.auth.LoginRequest;
import com.scasistemas.msbluedot.application.dto.auth.LoginResponse;
import com.scasistemas.msbluedot.application.services.RefreshTokenService;
import com.scasistemas.msbluedot.config.JwtProperties;
import com.scasistemas.msbluedot.config.SecurityProperties;
import com.scasistemas.msbluedot.domain.entities.AuditLog;
import com.scasistemas.msbluedot.domain.entities.Usuario;
import com.scasistemas.msbluedot.domain.exceptions.AccountLockedException;
import com.scasistemas.msbluedot.domain.repositories.IAuditLogRepository;
import com.scasistemas.msbluedot.domain.repositories.IUsuarioRepository;
import com.scasistemas.msbluedot.infrastructure.security.JwtTokenProvider;
import com.scasistemas.msbluedot.infrastructure.security.LoginAttemptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Use Case de autenticação com proteção contra brute force e audit log.
 *
 * <p>
 * Fluxo:
 * <ol>
 * <li>Verifica bloqueio da conta ({@link LoginAttemptService} + campo
 * {@code bloqueadoAte})</li>
 * <li>Autentica via Spring Security ({@link AuthenticationManager})</li>
 * <li>Em falha: incrementa contador, bloqueia se atingir limite, grava audit
 * log</li>
 * <li>Em sucesso: reseta contador, gera tokens, grava audit log</li>
 * </ol>
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticateUserUseCase {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;
    private final RefreshTokenService refreshTokenService;
    private final LoginAttemptService loginAttemptService;
    private final SecurityProperties securityProperties;
    private final IUsuarioRepository usuarioRepository;
    private final IAuditLogRepository auditLogRepository;

    /**
     * Autentica o usuário com nome e senha.
     *
     * @param request credenciais de login
     * @param ip      endereço IP do cliente (para audit log)
     * @return resposta com access e refresh tokens
     * @throws AccountLockedException  se a conta estiver bloqueada
     * @throws BadCredentialsException se as credenciais forem inválidas
     */
    @Transactional
    public LoginResponse execute(LoginRequest request, String ip) {
        String username = request.getNome();

        // 1. Verificar bloqueio em memória (cache de tentativas)
        if (loginAttemptService.isBlocked(username)) {
            log.warn("[Auth] Login bloqueado por brute force para '{}'", username);
            auditarFalha(username, null, ip, "LOGIN_BLOQUEADO", "Conta bloqueada por excesso de tentativas");
            throw new AccountLockedException(
                    "Conta temporariamente bloqueada. Aguarde e tente novamente.",
                    LocalDateTime.now().plusMinutes(15));
        }

        try {
            // 2. Autenticar (valida senha BCrypt via Spring Security)
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, request.getSenha()));

        } catch (AuthenticationException ex) {
            // 3. Falha de autenticação
            log.warn("[Auth] Credenciais inválidas para '{}'", username);
            loginAttemptService.loginFailed(username);

            // Bloquear conta no banco se atingiu limite
            usuarioRepository.findByNome(username).ifPresent(u -> {
                int max = securityProperties.getLogin().getMaxAttempts();
                long blockMs = securityProperties.getLogin().getBlockDuration();
                u.registrarFalhaLogin(max, blockMs);

                if (u.estaBloqueado()) {
                    log.warn("[Auth] Conta '{}' bloqueada até {}", username, u.getBloqueadoAte());
                }

                usuarioRepository.save(u);
            });

            auditarFalha(username, null, ip, "LOGIN_FALHA", "Credenciais inválidas");
            throw new BadCredentialsException("Credenciais inválidas");
        }

        // 4. Autenticação bem-sucedida — carregar dados completos
        Usuario usuario = usuarioRepository.findByNome(username)
                .orElseThrow(() -> new BadCredentialsException("Usuário não encontrado"));

        // 5. Verificar bloqueio persistido no banco
        if (usuario.estaBloqueado()) {
            throw new AccountLockedException(
                    "Conta bloqueada até " + usuario.getBloqueadoAte(),
                    usuario.getBloqueadoAte());
        }

        // 6. Resetar contadores
        loginAttemptService.loginSucceeded(username);
        usuario.registrarLoginSucesso();
        usuarioRepository.save(usuario);

        // 7. Gerar tokens
        String accessToken = jwtTokenProvider.generateAccessToken(usuario);
        String refreshToken = refreshTokenService.createRefreshToken(usuario);

        // 8. Audit log
        auditarSucesso(usuario, ip, "LOGIN", "Login realizado com sucesso");

        log.info("[Auth] Login bem-sucedido para '{}' (empresa={})", username, usuario.getIdEmpresa());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtProperties.getExpiration() / 1000)
                .refreshExpiresIn(jwtProperties.getRefreshExpiration() / 1000)
                .usuario(LoginResponse.UsuarioInfo.builder()
                        .id(usuario.getId())
                        .nome(usuario.getNome())
                        .role(usuario.getRole())
                        .idEmpresa(usuario.getIdEmpresa())
                        .build())
                .build();
    }

    // ─────────────────────────────────────────────────────────────────
    // Auditoria
    // ─────────────────────────────────────────────────────────────────

    private void auditarFalha(String username, Long idEmpresa, String ip,
            String acao, String detalhes) {
        try {
            auditLogRepository.save(AuditLog.builder()
                    .acao(acao)
                    .usuario(username)
                    .idEmpresa(idEmpresa)
                    .ip(ip)
                    .detalhes(detalhes)
                    .build());
        } catch (Exception e) {
            log.warn("[Audit] Falha ao gravar audit log: {}", e.getMessage());
        }
    }

    private void auditarSucesso(Usuario usuario, String ip, String acao, String detalhes) {
        auditarFalha(usuario.getNome(), usuario.getIdEmpresa(), ip, acao, detalhes);
    }
}

