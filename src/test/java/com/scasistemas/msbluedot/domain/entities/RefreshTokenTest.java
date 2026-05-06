package com.scasistemas.msbluedot.domain.entities;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("RefreshToken - Testes de domínio")
class RefreshTokenTest {

    private RefreshToken buildTokenValido() {
        return RefreshToken.builder()
                .id(1L)
                .idUsuario(10L)
                .tokenHash("sha256hashedvalue")
                .expiraEm(LocalDateTime.now().plusDays(7))
                .build();
    }

    @Test
    @DisplayName("Deve criar token com revogado=false por padrão")
    void deveCriarNaoRevogadoPorPadrao() {
        RefreshToken token = buildTokenValido();
        assertThat(token.getRevogado()).isFalse();
    }

    @Test
    @DisplayName("Deve estaValido=true quando não expirado e não revogado")
    void deveEstarValidoQuandoNaoExpiradoNaoRevogado() {
        RefreshToken token = buildTokenValido();
        assertThat(token.estaValido()).isTrue();
    }

    @Test
    @DisplayName("Deve estaValido=false quando expirado")
    void naoDeveEstarValidoQuandoExpirado() {
        RefreshToken token = RefreshToken.builder()
                .idUsuario(1L)
                .tokenHash("hash")
                .expiraEm(LocalDateTime.now().minusSeconds(1))
                .build();

        assertThat(token.estaValido()).isFalse();
    }

    @Test
    @DisplayName("Deve estaValido=false quando revogado")
    void naoDeveEstarValidoQuandoRevogado() {
        RefreshToken token = buildTokenValido();
        token.revogar();

        assertThat(token.estaValido()).isFalse();
        assertThat(token.getRevogado()).isTrue();
    }

    @Test
    @DisplayName("Deve estaValido=false quando expiraEm é null")
    void naoDeveEstarValidoQuandoExpiraEmNulo() {
        RefreshToken token = RefreshToken.builder()
                .idUsuario(1L)
                .tokenHash("hash")
                .expiraEm(null)
                .build();

        assertThat(token.estaValido()).isFalse();
    }

    @Test
    @DisplayName("Deve revogar corretamente")
    void deveRevogarCorretamente() {
        RefreshToken token = buildTokenValido();

        token.revogar();

        assertThat(token.getRevogado()).isTrue();
        assertThat(token.estaValido()).isFalse();
    }
}

