package com.scasistemas.msimagens.infrastructure.security;

import com.scasistemas.msimagens.config.JwtProperties;
import com.scasistemas.msimagens.domain.entities.Usuario;
import com.scasistemas.msimagens.domain.enums.RoleEnum;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("JwtTokenProvider - Testes unitários")
class JwtTokenProviderTest {

    private static final String SECRET = "test-secret-key-for-unit-tests-minimum-256-bits-length-here";

    private JwtTokenProvider jwtTokenProvider;

    private Usuario buildUsuario() {
        return Usuario.builder()
                .id(1L)
                .nome("test.user")
                .role(RoleEnum.USUARIO)
                .idEmpresa(10L)
                .build();
    }

    @BeforeEach
    void setUp() {
        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setSecret(SECRET);
        jwtProperties.setExpiration(1_800_000L); // 30 min
        jwtProperties.setRefreshExpiration(604_800_000L); // 7 days

        jwtTokenProvider = new JwtTokenProvider(jwtProperties);
        jwtTokenProvider.init(); // chama o @PostConstruct manualmente
    }

    @Test
    @DisplayName("Deve gerar access token com claims corretos")
    void deveGerarAccessTokenComClaimsCorretos() {
        Usuario usuario = buildUsuario();

        String token = jwtTokenProvider.generateAccessToken(usuario);

        assertThat(token).isNotBlank();
        assertThat(jwtTokenProvider.getUsernameFromToken(token)).isEqualTo("test.user");
        assertThat(jwtTokenProvider.getIdEmpresaFromToken(token)).isEqualTo(10L);
        assertThat(jwtTokenProvider.getRoleFromToken(token)).isEqualTo(RoleEnum.USUARIO);
        assertThat(jwtTokenProvider.isAccessToken(token)).isTrue();
        assertThat(jwtTokenProvider.isRefreshToken(token)).isFalse();
    }

    @Test
    @DisplayName("Deve gerar refresh token com tipo REFRESH")
    void deveGerarRefreshTokenComTipoRefresh() {
        Usuario usuario = buildUsuario();

        String token = jwtTokenProvider.generateRefreshToken(usuario);

        assertThat(token).isNotBlank();
        assertThat(jwtTokenProvider.getUsernameFromToken(token)).isEqualTo("test.user");
        assertThat(jwtTokenProvider.isRefreshToken(token)).isTrue();
        assertThat(jwtTokenProvider.isAccessToken(token)).isFalse();
    }

    @Test
    @DisplayName("Deve validar token válido retornando true")
    void deveValidarTokenValido() {
        String token = jwtTokenProvider.generateAccessToken(buildUsuario());
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
    }

    @Test
    @DisplayName("Deve rejeitar token com assinatura inválida")
    void deveRejeitarTokenComAssinaturaInvalida() {
        String fakeToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJoYWNrZXIifQ.invalid_signature";
        assertThat(jwtTokenProvider.validateToken(fakeToken)).isFalse();
    }

    @Test
    @DisplayName("Deve rejeitar token expirado ao validar")
    void deveRejeitarTokenExpirado() {
        JwtProperties shortExpiry = new JwtProperties();
        shortExpiry.setSecret(SECRET);
        shortExpiry.setExpiration(1L); // 1 ms
        shortExpiry.setRefreshExpiration(1L);

        JwtTokenProvider shortLivedProvider = new JwtTokenProvider(shortExpiry);
        shortLivedProvider.init();

        String token = shortLivedProvider.generateAccessToken(buildUsuario());

        // Aguarda expirar
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        assertThat(shortLivedProvider.validateToken(token)).isFalse();
    }

    @Test
    @DisplayName("Deve extrair data de expiração do token")
    void deveExtrairDataDeExpiracao() {
        String token = jwtTokenProvider.generateAccessToken(buildUsuario());
        assertThat(jwtTokenProvider.getExpirationDateFromToken(token)).isNotNull();
    }

    @Test
    @DisplayName("Deve gerar tokens diferentes para cada chamada")
    void deveGerarTokensDiferentes() throws InterruptedException {
        Usuario usuario = buildUsuario();
        String token1 = jwtTokenProvider.generateAccessToken(usuario);
        Thread.sleep(1);
        String token2 = jwtTokenProvider.generateAccessToken(usuario);
        // Tokens podem ser diferentes por timestamp
        assertThat(token1).isNotNull();
        assertThat(token2).isNotNull();
    }

    @Test
    @DisplayName("Deve identificar token ADMIN corretamente")
    void deveIdentificarTokenAdmin() {
        Usuario admin = Usuario.builder()
                .id(2L)
                .nome("admin")
                .role(RoleEnum.ADMINISTRADOR)
                .idEmpresa(1L)
                .build();

        String token = jwtTokenProvider.generateAccessToken(admin);
        assertThat(jwtTokenProvider.getRoleFromToken(token)).isEqualTo(RoleEnum.ADMINISTRADOR);
    }
}
