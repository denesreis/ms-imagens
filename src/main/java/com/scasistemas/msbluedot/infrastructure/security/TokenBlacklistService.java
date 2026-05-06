package com.scasistemas.msbluedot.infrastructure.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.scasistemas.msbluedot.infrastructure.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * Serviço de blacklist de tokens JWT para suporte a logout.
 *
 * <p>
 * Usa Caffeine Cache com TTL = tempo restante de expiração do token.
 * Tokens expirados são removidos automaticamente. A entry armazena
 * o epoch-millis da expiração (para debug/auditoria).
 * </p>
 *
 * <p>
 * Escopo: apenas access tokens são incluídos na blacklist.
 * Refresh tokens são invalidados no banco via
 * {@link com.scasistemas.msbluedot.application.services.RefreshTokenService}.
 * </p>
 */
@Slf4j
@Service
public class TokenBlacklistService {

    private final Cache<String, Long> blacklistCache;
    private final JwtTokenProvider jwtTokenProvider;

    public TokenBlacklistService(
            @Qualifier("blacklistCaffeineCache") Cache<String, Long> blacklistCache,
            JwtTokenProvider jwtTokenProvider) {
        this.blacklistCache = blacklistCache;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * Adiciona um token à blacklist.
     *
     * <p>
     * O cache tem TTL fixo de 31 minutos (configurado em
     * {@link com.scasistemas.msbluedot.config.CacheConfig}),
     * ligeiramente maior que o access token (30 min), cobrindo toda a vida útil do
     * token.
     * </p>
     *
     * @param token JWT de access token a invalidar
     */
    public void blacklistToken(String token) {
        try {
            Date expiration = jwtTokenProvider.getExpirationDateFromToken(token);
            long ttlMs = expiration.getTime() - System.currentTimeMillis();
            if (ttlMs > 0) {
                blacklistCache.put(token, expiration.getTime());
                log.debug("[Blacklist] Token adicionado, expira em {} ms", ttlMs);
            } else {
                // Token já expirado: não precisa adicionar à blacklist
                log.debug("[Blacklist] Token já expirado, ignorando blacklist");
            }
        } catch (Exception e) {
            // Token inválido: não precisa adicionar
            log.debug("[Blacklist] Token inválido, ignorando: {}", e.getMessage());
        }
    }

    /**
     * Verifica se um token está na blacklist.
     *
     * @param token JWT a verificar
     * @return {@code true} se o token foi invalidado por logout
     */
    public boolean isBlacklisted(String token) {
        return blacklistCache.getIfPresent(token) != null;
    }
}

