package com.scasistemas.msbluedot.application.services;

import com.scasistemas.msbluedot.application.dto.auth.LoginResponse;
import com.scasistemas.msbluedot.application.dto.auth.RefreshTokenResponse;
import com.scasistemas.msbluedot.config.JwtProperties;
import com.scasistemas.msbluedot.domain.entities.RefreshToken;
import com.scasistemas.msbluedot.domain.entities.Usuario;
import com.scasistemas.msbluedot.domain.exceptions.TokenRevokedException;
import com.scasistemas.msbluedot.domain.repositories.IRefreshTokenRepository;
import com.scasistemas.msbluedot.domain.repositories.IUsuarioRepository;
import com.scasistemas.msbluedot.infrastructure.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HexFormat;

/**
 * Serviço que gerencia o ciclo de vida dos refresh tokens.
 *
 * <ul>
 * <li>Criação: gera JWT + persiste hash SHA-256 no banco</li>
 * <li>Renovação: valida, rotaciona (invalida antigo, cria novo) e retorna novos
 * tokens</li>
 * <li>Revogação: marca como revogado (logout) ou revoga todos (segurança)</li>
 * </ul>
 *
 * <p>
 * Apenas o <strong>hash SHA-256</strong> é armazenado no banco —
 * nunca o token plain-text.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;
    private final IRefreshTokenRepository refreshTokenRepository;
    private final IUsuarioRepository usuarioRepository;

    // ─────────────────────────────────────────────────────────────────
    // Criação
    // ─────────────────────────────────────────────────────────────────

    /**
     * Gera um novo refresh token para o usuário, persiste seu hash no banco
     * e retorna o token em plain text para envio ao cliente.
     *
     * @param usuario usuário autenticado
     * @return token JWT (plain text) a entregar ao cliente
     */
    @Transactional
    public String createRefreshToken(Usuario usuario) {
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

    // ─────────────────────────────────────────────────────────────────
    // Renovação (rotação)
    // ─────────────────────────────────────────────────────────────────

    /**
     * Valida o refresh token, rotaciona (invalida antigo + cria novo) e
     * retorna novos tokens.
     *
     * @param rawRefreshToken token plain text recebido do cliente
     * @return {@link RefreshTokenResponse} com novo access token e refresh token
     * @throws TokenRevokedException se o token não existir, estiver revogado ou
     *                               expirado
     */
    @Transactional
    public RefreshTokenResponse refreshAccessToken(String rawRefreshToken) {
        String hash = sha256(rawRefreshToken);

        RefreshToken stored = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new TokenRevokedException("Refresh token não encontrado ou inválido"));

        if (!stored.estaValido()) {
            log.warn("[RefreshToken] Token inválido (revogado={}, expirado={})",
                    stored.getRevogado(), stored.getExpiraEm());
            throw new TokenRevokedException("Refresh token expirado ou revogado");
        }

        // Revogar o token antigo (rotação)
        stored.revogar();
        refreshTokenRepository.save(stored);

        // Carregar dados atualizados do usuário
        Usuario usuario = usuarioRepository.findById(stored.getIdUsuario())
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuário id=" + stored.getIdUsuario() + " não encontrado"));

        // Gerar novos tokens
        String newAccessToken = jwtTokenProvider.generateAccessToken(usuario);
        String newRefreshToken = createRefreshToken(usuario);

        log.info("[RefreshToken] Rotacionado para usuário '{}'", usuario.getNome());

        return RefreshTokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .expiresIn(jwtProperties.getExpiration() / 1000)
                .build();
    }

    // ─────────────────────────────────────────────────────────────────
    // Revogação
    // ─────────────────────────────────────────────────────────────────

    /**
     * Revoga um refresh token específico (logout).
     *
     * @param rawRefreshToken token plain text a revogar
     */
    @Transactional
    public void revokeRefreshToken(String rawRefreshToken) {
        String hash = sha256(rawRefreshToken);
        refreshTokenRepository.findByTokenHash(hash).ifPresent(t -> {
            t.revogar();
            refreshTokenRepository.save(t);
            log.debug("[RefreshToken] Revogado para usuário id={}", t.getIdUsuario());
        });
    }

    /**
     * Revoga todos os refresh tokens de um usuário (troca de senha, conta
     * comprometida).
     *
     * @param userId ID do usuário
     */
    @Transactional
    public void revokeAllUserTokens(Long userId) {
        refreshTokenRepository.revokeAllByUsuarioId(userId);
        log.info("[RefreshToken] Todos os tokens revogados para usuário id={}", userId);
    }

    // ─────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────

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

