package com.scasistemas.msimagens.domain.entities;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AuditLog - Testes unitários")
class AuditLogTest {

    @Test
    @DisplayName("Deve criar AuditLog via builder com todos os campos")
    void deveCriarAuditLogViaBuilder() {
        LocalDateTime agora = LocalDateTime.now();
        AuditLog log = AuditLog.builder()
                .id(1L)
                .acao("LOGIN")
                .usuario("admin")
                .idEmpresa(1L)
                .ip("192.168.1.1")
                .detalhes("Login realizado com sucesso")
                .dataCriacao(agora)
                .build();

        assertThat(log.getId()).isEqualTo(1L);
        assertThat(log.getAcao()).isEqualTo("LOGIN");
        assertThat(log.getUsuario()).isEqualTo("admin");
        assertThat(log.getIdEmpresa()).isEqualTo(1L);
        assertThat(log.getIp()).isEqualTo("192.168.1.1");
        assertThat(log.getDetalhes()).isEqualTo("Login realizado com sucesso");
        assertThat(log.getDataCriacao()).isEqualTo(agora);
    }

    @Test
    @DisplayName("Deve criar AuditLog com campos opcionais nulos")
    void deveCriarComCamposOpcionaisNulos() {
        AuditLog log = AuditLog.builder()
                .acao("CRIAR_PRODUTO")
                .usuario("user1")
                .build();

        assertThat(log.getId()).isNull();
        assertThat(log.getIdEmpresa()).isNull();
        assertThat(log.getIp()).isNull();
        assertThat(log.getDetalhes()).isNull();
        assertThat(log.getDataCriacao()).isNull();
    }

    @Test
    @DisplayName("Deve suportar diferentes ações de auditoria")
    void deveSuportarDiferentesAcoes() {
        String[] acoes = { "LOGIN", "LOGOUT", "LOGIN_FALHA", "CRIAR_USUARIO",
                "DELETAR_IMAGEM", "UPLOAD_IMAGEM", "ATUALIZAR_PRODUTO" };

        for (String acao : acoes) {
            AuditLog log = AuditLog.builder().acao(acao).usuario("test").build();
            assertThat(log.getAcao()).isEqualTo(acao);
        }
    }

    @Test
    @DisplayName("Deve permitir setter em todos os campos")
    void devePermitirSetters() {
        AuditLog log = new AuditLog();
        log.setId(10L);
        log.setAcao("LOGOUT");
        log.setUsuario("user1");
        log.setIdEmpresa(5L);
        log.setIp("10.0.0.1");
        log.setDetalhes("Detalhes do logout");
        log.setDataCriacao(LocalDateTime.now());

        assertThat(log.getId()).isEqualTo(10L);
        assertThat(log.getAcao()).isEqualTo("LOGOUT");
        assertThat(log.getUsuario()).isEqualTo("user1");
        assertThat(log.getIdEmpresa()).isEqualTo(5L);
    }
}
