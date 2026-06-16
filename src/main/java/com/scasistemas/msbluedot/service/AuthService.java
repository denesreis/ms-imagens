package com.scasistemas.msbluedot.service;

import com.scasistemas.msbluedot.config.JwtProperties;
import com.scasistemas.msbluedot.config.SecurityProperties;
import com.scasistemas.msbluedot.dto.LoginRequest;
import com.scasistemas.msbluedot.dto.LoginResponse;
import com.scasistemas.msbluedot.dto.RefreshTokenRequest;
import com.scasistemas.msbluedot.dto.RefreshTokenResponse;
import com.scasistemas.msbluedot.entity.AuditLog;
import com.scasistemas.msbluedot.entity.RefreshToken;
import com.scasistemas.msbluedot.entity.Usuario;
import com.scasistemas.msbluedot.exception.AccountLockedException;
import com.scasistemas.msbluedot.exception.TokenRevokedException;
import com.scasistemas.msbluedot.repository.AuditLogRepository;
import com.scasistemas.msbluedot.repository.RefreshTokenRepository;
import com.scasistemas.msbluedot.repository.UsuarioRepository;
import com.scasistemas.msbluedot.security.JwtTokenProvider;
import com.scasistemas.msbluedot.security.LoginAttemptService;
import com.scasistemas.msbluedot.security.TokenBlacklistService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;

@Slf4j
@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AuditLogRepository auditLogRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;
    private final SecurityProperties securityProperties;
    private final LoginAttemptService loginAttemptService;
    private final TokenBlacklistService tokenBlacklistService;

    public AuthService(
            AuthenticationManager authenticationManager,
            UsuarioRepository usuarioRepository,
            RefreshTokenRepository refreshTokenRepository,
            AuditLogRepository auditLogRepository,
            @Qualifier("newJwtTokenProvider") JwtTokenProvider jwtTokenProvider,
            JwtProperties jwtProperties,
            SecurityProperties securityProperties,
            @Qualifier("newLoginAttemptService") LoginAttemptService loginAttemptService,
            @Qualifier("newTokenBlacklistService") TokenBlacklistService tokenBlacklistService) {
        this.authenticationManager = authenticationManager;
        this.usuarioRepository = usuarioRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.auditLogRepository = auditLogRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.jwtProperties = jwtProperties;
        this.securityProperties = securityProperties;
        this.loginAttemptService = loginAttemptService;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Transactional
    public LoginResponse login(LoginRequest request, String ip) {
        String username = request.getNome();

        if (loginAttemptService.isBlocked(username)) {
            log.warn("[Auth] Login bloqueado por brute force para '{}'", username);
            auditarFalha(username, null, ip, "LOGIN_BLOQUEADO", "Conta bloqueada por excesso de tentativas");
            throw new AccountLockedException(
                    "Conta temporariamente bloqueada. Aguarde e tente novamente.",
                    LocalDateTime.now().plusMinutes(15));
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, request.getSenha()));
        } catch (AuthenticationException ex) {
            log.warn("[Auth] Credenciais inválidas para '{}'", username);
            loginAttemptService.loginFailed(username);

            usuarioRepository.findByNomeIgnoreCase(username).ifPresent(u -> {
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

        Usuario usuario = usuarioRepository.findByNomeIgnoreCase(username)
                .orElseThrow(() -> new BadCredentialsException("Usuário não encontrado"));

        if (usuario.estaBloqueado()) {
            throw new AccountLockedException(
                    "Conta bloqueada até " + usuario.getBloqueadoAte(),
                    usuario.getBloqueadoAte());
        }

        loginAttemptService.loginSucceeded(username);
        usuario.registrarLoginSucesso();
        usuarioRepository.save(usuario);

        String accessToken = jwtTokenProvider.generateAccessToken(usuario);
        String rawRefreshToken = criarRefreshToken(usuario);

        auditarFalha(username, usuario.getIdEmpresa(), ip, "LOGIN", "Login realizado com sucesso");
        log.info("[Auth] Login bem-sucedido para '{}' (empresa={})", username, usuario.getIdEmpresa());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(rawRefreshToken)
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

    @Transactional
    public RefreshTokenResponse refresh(RefreshTokenRequest request) {
        String hash = sha256(request.getRefreshToken());

        RefreshToken stored = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new TokenRevokedException("Refresh token não encontrado ou inválido"));

        if (!stored.estaValido()) {
            log.warn("[RefreshToken] Token inválido (revogado={}, expirado={})",
                    stored.getRevogado(), stored.getExpiraEm());
            throw new TokenRevokedException("Refresh token expirado ou revogado");
        }

        stored.revogar();
        refreshTokenRepository.save(stored);

        Usuario usuario = usuarioRepository.findById(stored.getIdUsuario())
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuário id=" + stored.getIdUsuario() + " não encontrado"));

        String newAccessToken = jwtTokenProvider.generateAccessToken(usuario);
        String newRefreshToken = criarRefreshToken(usuario);

        log.info("[RefreshToken] Rotacionado para usuário '{}'", usuario.getNome());

        return RefreshTokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .expiresIn(jwtProperties.getExpiration() / 1000)
                .build();
    }

    @Transactional
    public void logout(HttpServletRequest httpReq, String rawRefreshToken,
                       String username, Long idEmpresa, String ip) {
        String authHeader = httpReq.getHeader("Authorization");
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            String accessToken = authHeader.substring(7);
            tokenBlacklistService.blacklistToken(accessToken);
            log.debug("[Logout] Access token adicionado à blacklist");
        }

        if (StringUtils.hasText(rawRefreshToken)) {
            String hash = sha256(rawRefreshToken);
            refreshTokenRepository.findByTokenHash(hash).ifPresent(t -> {
                t.revogar();
                refreshTokenRepository.save(t);
                log.debug("[Logout] Refresh token revogado para usuário id={}", t.getIdUsuario());
            });
        }

        SecurityContextHolder.clearContext();

        try {
            auditLogRepository.save(AuditLog.builder()
                    .acao("LOGOUT")
                    .usuario(username)
                    .idEmpresa(idEmpresa)
                    .ip(ip)
                    .detalhes("Logout realizado com sucesso")
                    .build());
        } catch (Exception e) {
            log.warn("[Audit] Falha ao gravar audit log de logout: {}", e.getMessage());
        }

        log.info("[Logout] Usuário '{}' deslogado com sucesso", username);
    }

    @Transactional
    public String criarRefreshToken(Usuario usuario) {
        String rawToken = jwtTokenProvider.generateRefreshToken(usuario);
        String hash = sha256(rawToken);

        LocalDateTime expiry = LocalDateTime.now()
                .plusSeconds(jwtProperties.getRefreshExpiration() / 1000);

        RefreshToken entity = RefreshToken.builder()
                .idUsuario(usuario.getId())
                .tokenHash(hash)
                .expiraEm(expiry)
                .revogado(false)
                .build();

        refreshTokenRepository.save(entity);
        log.debug("[RefreshToken] Criado para usuário id={}", usuario.getId());
        return rawToken;
    }

    @Transactional
    public void revogarTodosTokensDoUsuario(Long userId) {
        refreshTokenRepository.revokeAllByIdUsuario(userId);
        log.info("[RefreshToken] Todos os tokens revogados para usuário id={}", userId);
    }

    private void auditarFalha(String username, Long idEmpresa, String ip, String acao, String detalhes) {
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

    private String sha256(String input) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 não disponível", e);
        }
    }
}
