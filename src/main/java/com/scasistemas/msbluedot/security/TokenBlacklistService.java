package com.scasistemas.msbluedot.security;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Date;

@Slf4j
@Service("newTokenBlacklistService")
public class TokenBlacklistService {

    private final Cache<String, Long> blacklistCache;
    private final JwtTokenProvider jwtTokenProvider;

    public TokenBlacklistService(
            @Qualifier("blacklistCaffeineCache") Cache<String, Long> blacklistCache,
            @Qualifier("newJwtTokenProvider") JwtTokenProvider jwtTokenProvider) {
        this.blacklistCache = blacklistCache;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public void blacklistToken(String token) {
        try {
            Date expiration = jwtTokenProvider.getExpirationDateFromToken(token);
            long ttlMs = expiration.getTime() - System.currentTimeMillis();
            if (ttlMs > 0) {
                blacklistCache.put(token, expiration.getTime());
                log.debug("[Blacklist] Token adicionado, expira em {} ms", ttlMs);
            }
        } catch (Exception e) {
            log.debug("[Blacklist] Token inválido, ignorando: {}", e.getMessage());
        }
    }

    public boolean isBlacklisted(String token) {
        return blacklistCache.getIfPresent(token) != null;
    }
}
