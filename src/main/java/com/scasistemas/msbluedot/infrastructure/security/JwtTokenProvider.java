package com.scasistemas.msbluedot.infrastructure.security;

import com.scasistemas.msbluedot.config.JwtProperties;
import com.scasistemas.msbluedot.domain.entities.Usuario;
import com.scasistemas.msbluedot.domain.enums.RoleEnum;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Componente responsável por geração e validação de JWTs.
 *
 * <p>
 * Claims do access token:
 * <ul>
 * <li>{@code sub} — nome do usuário</li>
 * <li>{@code idEmpresa} — ID da empresa</li>
 * <li>{@code role} — perfil de acesso</li>
 * <li>{@code tokenType} — ACCESS ou REFRESH</li>
 * </ul>
 * </p>
 *
 * <p>
 * <strong>Nota:</strong> a verificação de blacklist é responsabilidade do
 * {@link JwtAuthenticationFilter}, não deste componente.
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private static final String CLAIM_ID_EMPRESA = "idEmpresa";
    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_TOKEN_TYPE = "tokenType";
    private static final String TOKEN_TYPE_ACCESS = "ACCESS";
    private static final String TOKEN_TYPE_REFRESH = "REFRESH";

    private final JwtProperties jwtProperties;
    private SecretKey signingKey;

    @PostConstruct
    void init() {
        byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        // Keys.hmacShaKeyFor valida o tamanho mínimo (>=256 bits para HS256)
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    // ─────────────────────────────────────────────────────────────────
    // Geração de tokens
    // ─────────────────────────────────────────────────────────────────

    /**
     * Gera um access token JWT de curta duração (30 min padrão).
     */
    public String generateAccessToken(Usuario usuario) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtProperties.getExpiration());

        return Jwts.builder()
                .subject(usuario.getNome())
                .claim(CLAIM_ID_EMPRESA, usuario.getIdEmpresa())
                .claim(CLAIM_ROLE, usuario.getRole().name())
                .claim(CLAIM_TOKEN_TYPE, TOKEN_TYPE_ACCESS)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey)
                .compact();
    }

    /**
     * Gera um refresh token JWT de longa duração (7 dias padrão).
     *
     * <p>
     * O token gerado aqui é o valor que o cliente armazena.
     * O {@link com.scasistemas.msbluedot.application.services.RefreshTokenService}
     * calcula SHA-256 deste valor antes de persistir no banco.
     * </p>
     */
    public String generateRefreshToken(Usuario usuario) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtProperties.getRefreshExpiration());

        return Jwts.builder()
                .subject(usuario.getNome())
                .claim(CLAIM_ID_EMPRESA, usuario.getIdEmpresa())
                .claim(CLAIM_TOKEN_TYPE, TOKEN_TYPE_REFRESH)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey)
                .compact();
    }

    // ─────────────────────────────────────────────────────────────────
    // Validação
    // ─────────────────────────────────────────────────────────────────

    /**
     * Verifica assinatura e expiração do token.
     *
     * <p>
     * <strong>Não verifica blacklist</strong> — isso é feito pelo filtro.
     * </p>
     */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.debug("[JWT] Token expirado: {}", e.getMessage());
        } catch (JwtException e) {
            log.warn("[JWT] Token inválido: {}", e.getMessage());
        } catch (Exception e) {
            log.warn("[JWT] Erro ao validar token: {}", e.getMessage());
        }
        return false;
    }

    // ─────────────────────────────────────────────────────────────────
    // Extração de claims
    // ─────────────────────────────────────────────────────────────────

    public String getUsernameFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    public Long getIdEmpresaFromToken(String token) {
        Object value = parseClaims(token).get(CLAIM_ID_EMPRESA);
        if (value instanceof Number n)
            return n.longValue();
        return null;
    }

    public RoleEnum getRoleFromToken(String token) {
        String roleStr = parseClaims(token).get(CLAIM_ROLE, String.class);
        if (roleStr == null)
            return null;
        try {
            return RoleEnum.valueOf(roleStr);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public Date getExpirationDateFromToken(String token) {
        return parseClaims(token).getExpiration();
    }

    public String getTokenTypeFromToken(String token) {
        return parseClaims(token).get(CLAIM_TOKEN_TYPE, String.class);
    }

    public boolean isAccessToken(String token) {
        return TOKEN_TYPE_ACCESS.equals(getTokenTypeFromToken(token));
    }

    public boolean isRefreshToken(String token) {
        return TOKEN_TYPE_REFRESH.equals(getTokenTypeFromToken(token));
    }

    // ─────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}

