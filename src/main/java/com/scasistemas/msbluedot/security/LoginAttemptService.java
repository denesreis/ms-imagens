package com.scasistemas.msbluedot.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.scasistemas.msbluedot.config.SecurityProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Slf4j
@Service("newLoginAttemptService")
public class LoginAttemptService {

    private final Cache<String, Integer> attemptsCache;
    private final SecurityProperties securityProperties;

    public LoginAttemptService(
            @Qualifier("loginAttemptsCaffeineCache") Cache<String, Integer> attemptsCache,
            SecurityProperties securityProperties) {
        this.attemptsCache = attemptsCache;
        this.securityProperties = securityProperties;
    }

    public void loginFailed(String username) {
        int current = getAttempts(username);
        int updated = current + 1;
        attemptsCache.put(username, updated);
        int max = securityProperties.getLogin().getMaxAttempts();
        log.warn("[BruteForce] Falha de login para '{}': tentativa {}/{}", username, updated, max);
        if (updated >= max) {
            log.warn("[BruteForce] Conta '{}' atingiu limite de tentativas - será bloqueada", username);
        }
    }

    public void loginSucceeded(String username) {
        attemptsCache.invalidate(username);
        log.debug("[BruteForce] Contador resetado para '{}'", username);
    }

    public boolean isBlocked(String username) {
        return getAttempts(username) >= securityProperties.getLogin().getMaxAttempts();
    }

    public int getAttempts(String username) {
        Integer count = attemptsCache.getIfPresent(username);
        return count != null ? count : 0;
    }
}
