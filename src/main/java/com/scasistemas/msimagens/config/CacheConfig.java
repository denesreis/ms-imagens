package com.scasistemas.msimagens.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.util.concurrent.TimeUnit;

/**
 * Configuração do Caffeine Cache para:
 * <ul>
 * <li>{@code token-blacklist} — tokens JWT invalidados por logout</li>
 * <li>{@code login-attempts} — contagem de tentativas de login (brute
 * force)</li>
 * </ul>
 *
 * <p>
 * O Spring Boot {@code @EnableCaching} está ativado em
 * {@link com.scasistemas.msimagens.MsImagensApplication}.
 * </p>
 */
@Configuration
public class CacheConfig {

    /** Nome do cache de blacklist de tokens JWT. */
    public static final String CACHE_TOKEN_BLACKLIST = "token-blacklist";

    /** Nome do cache de tentativas de login. */
    public static final String CACHE_LOGIN_ATTEMPTS = "login-attempts";

    /** Nome do cache de imagens por código EAN (endpoint público). */
    public static final String CACHE_IMAGENS_POR_EAN = "imagensPorEan";

    /**
     * Cache manager primário usado pelo {@code @Cacheable} padrão.
     *
     * <p>
     * TTLs configurados:
     * <ul>
     * <li>{@code token-blacklist}: 31 minutos (> access token de 30 min)</li>
     * <li>{@code login-attempts}: 15 minutos (janela de brute force)</li>
     * </ul>
     * </p>
     */
    @Primary
    @Bean
    @Profile("!test")
    public CacheManager cacheManager() {
        CaffeineCacheManager mgr = new CaffeineCacheManager(
                CACHE_TOKEN_BLACKLIST,
                CACHE_LOGIN_ATTEMPTS,
                CACHE_IMAGENS_POR_EAN);
        mgr.setCaffeine(defaultCaffeineSpec());
        return mgr;
    }

    /**
     * Cache dedicado para a blacklist de tokens — TTL de 31 minutos.
     * O TTL deve ser ligeiramente maior que o access token (30 min)
     * para garantir cobertura completa.
     */
    @Bean("blacklistCaffeineCache")
    public com.github.benmanes.caffeine.cache.Cache<String, Long> blacklistCache() {
        return Caffeine.newBuilder()
                .expireAfterWrite(31, TimeUnit.MINUTES)
                .maximumSize(50_000)
                .build();
    }

    /**
     * Cache dedicado para contagem de tentativas de login — TTL de 15 minutos.
     */
    @Bean("loginAttemptsCaffeineCache")
    public com.github.benmanes.caffeine.cache.Cache<String, Integer> loginAttemptsCache() {
        return Caffeine.newBuilder()
                .expireAfterWrite(15, TimeUnit.MINUTES)
                .maximumSize(10_000)
                .build();
    }

    private Caffeine<Object, Object> defaultCaffeineSpec() {
        return Caffeine.newBuilder()
                .expireAfterWrite(31, TimeUnit.MINUTES)
                .maximumSize(50_000);
    }
}
