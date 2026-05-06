package com.scasistemas.msbluedot.domain.entities;

import com.scasistemas.msbluedot.domain.enums.RoleEnum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Usuario - Testes de domínio")
class UsuarioTest {

    private Usuario buildUsuario() {
        return Usuario.builder()
                .id(1L)
                .idEmpresa(10L)
                .nome("joao.silva")
                .senha("$2a$12$hashedPassword")
                .role(RoleEnum.USUARIO)
                .build();
    }

    @Test
    @DisplayName("Deve criar usuário com valores padrão corretos")
    void deveCriarComValoresPadrao() {
        Usuario usuario = buildUsuario();

        assertThat(usuario.getAtivo()).isTrue();
        assertThat(usuario.getTentativasLogin()).isEqualTo(0);
        assertThat(usuario.getBloqueadoAte()).isNull();
        assertThat(usuario.estaBloqueado()).isFalse();
    }

    @Test
    @DisplayName("Deve retornar estaBloqueado=false quando bloqueadoAte é null")
    void naoDeveEstarBloqueadoQuandoBloqueadoAteNulo() {
        Usuario usuario = buildUsuario();
        assertThat(usuario.estaBloqueado()).isFalse();
    }

    @Test
    @DisplayName("Deve retornar estaBloqueado=true quando bloqueadoAte está no futuro")
    void deveEstarBloqueadoQuandoBloqueadoAteNoFuturo() {
        Usuario usuario = buildUsuario();
        usuario.setBloqueadoAte(LocalDateTime.now().plusMinutes(10));
        assertThat(usuario.estaBloqueado()).isTrue();
    }

    @Test
    @DisplayName("Deve retornar estaBloqueado=false quando bloqueadoAte está no passado")
    void naoDeveEstarBloqueadoQuandoBloqueadoAteNoPassado() {
        Usuario usuario = buildUsuario();
        usuario.setBloqueadoAte(LocalDateTime.now().minusMinutes(1));
        assertThat(usuario.estaBloqueado()).isFalse();
    }

    @Test
    @DisplayName("Deve incrementar tentativas de login e bloquear após MAX")
    void deveIncrementarTentativasEBloquear() {
        Usuario usuario = buildUsuario();
        int maxTentativas = 3;
        long duracaoMs = 15 * 60 * 1000L;

        usuario.registrarFalhaLogin(maxTentativas, duracaoMs);
        assertThat(usuario.getTentativasLogin()).isEqualTo(1);
        assertThat(usuario.estaBloqueado()).isFalse();

        usuario.registrarFalhaLogin(maxTentativas, duracaoMs);
        assertThat(usuario.getTentativasLogin()).isEqualTo(2);
        assertThat(usuario.estaBloqueado()).isFalse();

        usuario.registrarFalhaLogin(maxTentativas, duracaoMs);
        assertThat(usuario.getTentativasLogin()).isEqualTo(3);
        assertThat(usuario.estaBloqueado()).isTrue();
        assertThat(usuario.getBloqueadoAte()).isAfter(LocalDateTime.now());
    }

    @Test
    @DisplayName("Deve zerar tentativas e bloqueio após login bem-sucedido")
    void deveZerarTentativasAposLoginSucesso() {
        Usuario usuario = buildUsuario();
        usuario.setTentativasLogin(4);
        usuario.setBloqueadoAte(LocalDateTime.now().plusMinutes(10));

        usuario.registrarLoginSucesso();

        assertThat(usuario.getTentativasLogin()).isEqualTo(0);
        assertThat(usuario.getBloqueadoAte()).isNull();
        assertThat(usuario.estaBloqueado()).isFalse();
    }

    @Test
    @DisplayName("Deve ter o perfil correto")
    void deveTerPerfilCorreto() {
        Usuario admin = Usuario.builder()
                .nome("admin")
                .role(RoleEnum.ADMINISTRADOR)
                .build();

        assertThat(admin.getRole()).isEqualTo(RoleEnum.ADMINISTRADOR);
    }
}

