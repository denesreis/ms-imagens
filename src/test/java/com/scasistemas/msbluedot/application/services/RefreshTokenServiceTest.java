package com.scasistemas.msbluedot.application.services;

import com.scasistemas.msbluedot.application.dto.auth.RefreshTokenResponse;
import com.scasistemas.msbluedot.config.JwtProperties;
import com.scasistemas.msbluedot.domain.entities.RefreshToken;
import com.scasistemas.msbluedot.domain.entities.Usuario;
import com.scasistemas.msbluedot.domain.enums.RoleEnum;
import com.scasistemas.msbluedot.domain.exceptions.TokenRevokedException;
import com.scasistemas.msbluedot.domain.repositories.IRefreshTokenRepository;
import com.scasistemas.msbluedot.domain.repositories.IUsuarioRepository;
import com.scasistemas.msbluedot.infrastructure.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RefreshTokenService - Testes unitários")
class RefreshTokenServiceTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private JwtProperties jwtProperties;
    @Mock
    private IRefreshTokenRepository refreshTokenRepository;
    @Mock
    private IUsuarioRepository usuarioRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = Usuario.builder()
                .id(1L)
                .nome("admin")
                .role(RoleEnum.ADMINISTRADOR)
                .idEmpresa(1L)
                .ativo(true)
                .build();
    }

    @Test
    @DisplayName("Deve criar refresh token e persistir hash SHA-256")
    void deveCriarRefreshToken() {
        when(jwtTokenProvider.generateRefreshToken(usuario)).thenReturn("refresh-jwt-string");
        when(jwtProperties.getRefreshExpiration()).thenReturn(604_800_000L);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));

        String token = refreshTokenService.createRefreshToken(usuario);

        assertThat(token).isEqualTo("refresh-jwt-string");
        verify(refreshTokenRepository).save(argThat(rt -> rt.getIdUsuario().equals(1L) &&
                rt.getTokenHash() != null &&
                !rt.getTokenHash().isEmpty() &&
                !rt.getRevogado()));
    }

    @Test
    @DisplayName("Deve renovar access token com refresh token válido (rotação)")
    void deveRenovarAccessTokenComRotacao() {
        RefreshToken storedToken = RefreshToken.builder()
                .id(1L)
                .idUsuario(1L)
                .tokenHash("some-hash")
                .revogado(false)
                .expiraEm(LocalDateTime.now().plusDays(1))
                .build();

        // refreshAccessToken calcula SHA-256 do token cru e busca no repo
        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(storedToken));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        // Mock para geração de novos tokens
        when(jwtTokenProvider.generateAccessToken(usuario)).thenReturn("new-access-token");
        when(jwtTokenProvider.generateRefreshToken(usuario)).thenReturn("new-refresh-token");
        when(jwtProperties.getRefreshExpiration()).thenReturn(604_800_000L);
        when(jwtProperties.getExpiration()).thenReturn(1_800_000L);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));

        RefreshTokenResponse response = refreshTokenService.refreshAccessToken("old-refresh");

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("new-access-token");
        assertThat(response.getRefreshToken()).isEqualTo("new-refresh-token");
        // Verify old token revoked
        assertThat(storedToken.getRevogado()).isTrue();
    }

    @Test
    @DisplayName("Deve lançar exceção para refresh token revogado")
    void deveLancarExcecaoParaTokenRevogado() {
        RefreshToken storedToken = RefreshToken.builder()
                .id(1L)
                .idUsuario(1L)
                .tokenHash("some-hash")
                .revogado(true) // JÁ REVOGADO
                .expiraEm(LocalDateTime.now().plusDays(1))
                .build();

        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(storedToken));

        assertThatThrownBy(() -> refreshTokenService.refreshAccessToken("revoked-refresh"))
                .isInstanceOf(TokenRevokedException.class);
    }

    @Test
    @DisplayName("Deve revogar refresh token individual")
    void deveRevogarRefreshTokenIndividual() {
        RefreshToken storedToken = RefreshToken.builder()
                .id(1L)
                .idUsuario(1L)
                .tokenHash("some-hash")
                .revogado(false)
                .build();

        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(storedToken));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(storedToken);

        refreshTokenService.revokeRefreshToken("some-token");

        assertThat(storedToken.getRevogado()).isTrue();
        verify(refreshTokenRepository).save(storedToken);
    }

    @Test
    @DisplayName("Deve revogar todos os tokens de um usuário")
    void deveRevogarTodosTokensDeUmUsuario() {
        refreshTokenService.revokeAllUserTokens(1L);

        verify(refreshTokenRepository).revokeAllByUsuarioId(1L);
    }
}

