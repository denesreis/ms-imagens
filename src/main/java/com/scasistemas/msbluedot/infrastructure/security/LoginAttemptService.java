package com.scasistemas.msbluedot.infrastructure.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.scasistemas.msbluedot.config.SecurityProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Serviço de proteção conta brute force no login.
 *
 * <p>
 * Rastreia tentativas de login falhas por username usando Caffeine Cache
 * com TTL de 15 minutos (janela deslizante reset automático pelo cache).
 * </p>
 *
 * <p>
 * Após {@code maxAttempts} tentativas o campo {@code bloqueadoAte} é
 * definido pelo
 * {@link com.scasistemas.msbluedot.application.usecases.AuthenticateUserUseCase},
 * que por sua vez usa os métodos de domínio de
 * {@link com.scasistemas.msbluedot.domain.entities.Usuario}.
 * </p>
 */
@Slf4j
@Service
public class LoginAttemptService {

    private final Cache<String, Integer> attemptsCache;
    private final SecurityProperties securityProperties;

    public LoginAttemptService(
            @Qualifier("loginAttemptsCaffeineCache") Cache<String, Integer> attemptsCache,
            SecurityProperties securityProperties) {
        this.attemptsCache = attemptsCache;
        this.securityProperties = securityProperties;
    }

    /**
     * Registra uma falha de login para o username.
     * Incrementa o contador; se atingir o máximo, faz log de aviso.
     *
     * @param username nome do usuário que falhou
     */
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

    /**
     * Reseta o contador após login bem-sucedido.
     *
     * @param username nome do usuário que fez login com sucesso
     */
    public void loginSucceeded(String username) {
        attemptsCache.invalidate(username);
        log.debug("[BruteForce] Contador resetado para '{}'", username);
    }

    /**
     * Verifica se o username atingiu o número máximo de tentativas no cache.
     *
     * <p>
     * Observação: o bloqueio definitivo é verificado pelo campo
     * {@code bloqueadoAte} na entidade
     * {@link com.scasistemas.msbluedot.domain.entities.Usuario}.
     * Este método verifica apenas a janela de tentativas em memória.
     * </p>
     *
     * @param username nome do usuário
     * @return {@code true} se o limite de tentativas foi atingido no cache
     */
    public boolean isBlocked(String username) {
        return getAttempts(username) >= securityProperties.getLogin().getMaxAttempts();
    }

    /**
     * Retorna o número atual de tentativas falhas para o username.
     */
    public int getAttempts(String username) {
        Integer count = attemptsCache.getIfPresent(username);
        return count != null ? count : 0;
    }
}

