package com.scasistemas.msimagens.infrastructure.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.scasistemas.msimagens.config.SecurityProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("LoginAttemptService - Testes unitários")
class LoginAttemptServiceTest {

    private LoginAttemptService loginAttemptService;
    private SecurityProperties securityProperties;

    @BeforeEach
    void setUp() {
        Cache<String, Integer> attemptsCache = Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(15, TimeUnit.MINUTES)
                .build();

        securityProperties = new SecurityProperties();
        securityProperties.getLogin().setMaxAttempts(5);
        securityProperties.getLogin().setBlockDuration(900_000L);

        loginAttemptService = new LoginAttemptService(attemptsCache, securityProperties);
    }

    @Test
    @DisplayName("Deve iniciar com zero tentativas")
    void deveIniciarComZeroTentativas() {
        assertThat(loginAttemptService.isBlocked("user1")).isFalse();
    }

    @Test
    @DisplayName("Deve incrementar contagem a cada falha de login")
    void deveIncrementarContagemAoCadaFalha() {
        loginAttemptService.loginFailed("user1");
        loginAttemptService.loginFailed("user1");
        loginAttemptService.loginFailed("user1");

        // 3 tentativas < 5 (max): não bloqueado ainda
        assertThat(loginAttemptService.isBlocked("user1")).isFalse();
    }

    @Test
    @DisplayName("Deve bloquear após MAX_ATTEMPTS (5) falhas")
    void deveBloquearAposMaxAttempts() {
        for (int i = 0; i < 5; i++) {
            loginAttemptService.loginFailed("user_block");
        }

        assertThat(loginAttemptService.isBlocked("user_block")).isTrue();
    }

    @Test
    @DisplayName("Deve desbloquear após loginSucceeded")
    void deveDesbloquearAposLoginSucceeded() {
        for (int i = 0; i < 5; i++) {
            loginAttemptService.loginFailed("user_reset");
        }
        assertThat(loginAttemptService.isBlocked("user_reset")).isTrue();

        loginAttemptService.loginSucceeded("user_reset");

        assertThat(loginAttemptService.isBlocked("user_reset")).isFalse();
    }

    @Test
    @DisplayName("Deve retornar false para usuário que nunca tentou login")
    void deveRetornarFalseParaNovoUsuario() {
        assertThat(loginAttemptService.isBlocked("usuario_novo")).isFalse();
    }

    @Test
    @DisplayName("Deve rastrear tentativas por usuário separadamente")
    void deveRastrearPorUsuarioSeparadamente() {
        for (int i = 0; i < 5; i++) {
            loginAttemptService.loginFailed("user_a");
        }
        loginAttemptService.loginFailed("user_b");

        assertThat(loginAttemptService.isBlocked("user_a")).isTrue();
        assertThat(loginAttemptService.isBlocked("user_b")).isFalse();
    }

    @Test
    @DisplayName("Deve respeitar configuração de maxAttempts customizado")
    void deveRespetirarCustomMaxAttempts() {
        securityProperties.getLogin().setMaxAttempts(3);

        loginAttemptService.loginFailed("user_custom");
        loginAttemptService.loginFailed("user_custom");
        assertThat(loginAttemptService.isBlocked("user_custom")).isFalse();

        loginAttemptService.loginFailed("user_custom");
        assertThat(loginAttemptService.isBlocked("user_custom")).isTrue();
    }
}
