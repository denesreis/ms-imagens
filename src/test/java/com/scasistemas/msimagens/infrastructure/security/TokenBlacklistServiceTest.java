package com.scasistemas.msimagens.infrastructure.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.scasistemas.msimagens.config.JwtProperties;
import com.scasistemas.msimagens.domain.entities.Usuario;
import com.scasistemas.msimagens.domain.enums.RoleEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TokenBlacklistService - Testes unitários")
class TokenBlacklistServiceTest {

    private static final String SECRET = "test-secret-key-for-unit-tests-minimum-256-bits-length-here";

    private TokenBlacklistService tokenBlacklistService;
    private JwtTokenProvider jwtTokenProvider;

    private Usuario buildUsuario() {
        return Usuario.builder()
                .id(1L)
                .nome("test.user")
                .role(RoleEnum.USUARIO)
                .idEmpresa(1L)
                .build();
    }

    @BeforeEach
    void setUp() {
        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setSecret(SECRET);
        jwtProperties.setExpiration(1_800_000L);
        jwtProperties.setRefreshExpiration(604_800_000L);

        jwtTokenProvider = new JwtTokenProvider(jwtProperties);
        jwtTokenProvider.init();

        Cache<String, Long> blacklistCache = Caffeine.newBuilder()
                .maximumSize(10_000)
                .expireAfterWrite(31, TimeUnit.MINUTES)
                .build();

        tokenBlacklistService = new TokenBlacklistService(blacklistCache, jwtTokenProvider);
    }

    @Test
    @DisplayName("Token recém gerado não deve estar na blacklist")
    void tokenNovaNaoDeveEstarNaBlacklist() {
        String token = jwtTokenProvider.generateAccessToken(buildUsuario());
        assertThat(tokenBlacklistService.isBlacklisted(token)).isFalse();
    }

    @Test
    @DisplayName("Deve adicionar token à blacklist após blacklistToken")
    void deveAdicionarTokenABlacklist() {
        String token = jwtTokenProvider.generateAccessToken(buildUsuario());

        tokenBlacklistService.blacklistToken(token);

        assertThat(tokenBlacklistService.isBlacklisted(token)).isTrue();
    }

    @Test
    @DisplayName("Não deve blacklistar token já expirado")
    void naoDeveBlacklistarTokenExpirado() {
        JwtProperties shortExpiry = new JwtProperties();
        shortExpiry.setSecret(SECRET);
        shortExpiry.setExpiration(1L);
        shortExpiry.setRefreshExpiration(1L);

        JwtTokenProvider shortProvider = new JwtTokenProvider(shortExpiry);
        shortProvider.init();

        Cache<String, Long> cache = Caffeine.newBuilder()
                .maximumSize(100)
                .expireAfterWrite(1, TimeUnit.MINUTES)
                .build();

        TokenBlacklistService service = new TokenBlacklistService(cache, shortProvider);

        String token = shortProvider.generateAccessToken(buildUsuario());
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        service.blacklistToken(token); // token já expirado

        assertThat(service.isBlacklisted(token)).isFalse();
    }

    @Test
    @DisplayName("Deve gerenciar blacklist de múltiplos tokens")
    void deveGerenciarMultiplosTokens() {
        Usuario usuario2 = Usuario.builder()
                .id(2L)
                .nome("other.user")
                .role(RoleEnum.USUARIO)
                .idEmpresa(1L)
                .build();

        String token1 = jwtTokenProvider.generateAccessToken(buildUsuario());
        String token2 = jwtTokenProvider.generateAccessToken(usuario2);

        tokenBlacklistService.blacklistToken(token1);

        assertThat(tokenBlacklistService.isBlacklisted(token1)).isTrue();
        assertThat(tokenBlacklistService.isBlacklisted(token2)).isFalse();
    }

    @Test
    @DisplayName("Deve ignorar graciosa token inválido/malformado")
    void deveIgnorarTokenInvalido() {
        // Não deve lançar exceção
        tokenBlacklistService.blacklistToken("token.invalido.malformado");
        assertThat(tokenBlacklistService.isBlacklisted("token.invalido.malformado")).isFalse();
    }
}
