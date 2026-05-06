package com.scasistemas.msbluedot.domain.entities;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Empresa - Testes de domínio")
class EmpresaTest {

    @Test
    @DisplayName("Deve criar empresa com ativo=true por padrão")
    void deveCriarComAtivoTrue() {
        Empresa empresa = Empresa.builder()
                .codigoErp("ERP-001")
                .nome("Empresa Teste S.A.")
                .build();

        assertThat(empresa.getAtivo()).isTrue();
        assertThat(empresa.getNome()).isEqualTo("Empresa Teste S.A.");
        assertThat(empresa.getCodigoErp()).isEqualTo("ERP-001");
    }

    @Test
    @DisplayName("Deve construir empresa com todos os campos")
    void deveConstruirComTodosCampos() {
        Empresa empresa = Empresa.builder()
                .id(1L)
                .codigoErp("ERP-999")
                .nome("Empresa Completa LTDA")
                .ativo(true)
                .build();

        assertThat(empresa.getId()).isEqualTo(1L);
        assertThat(empresa.getCodigoErp()).isEqualTo("ERP-999");
        assertThat(empresa.getNome()).isEqualTo("Empresa Completa LTDA");
    }

    @Test
    @DisplayName("Deve permitir soft delete marcando ativo=false")
    void deveSoftDelete() {
        Empresa empresa = Empresa.builder()
                .codigoErp("ERP-001")
                .nome("Empresa")
                .build();

        empresa.setAtivo(false);

        assertThat(empresa.getAtivo()).isFalse();
    }

    @Test
    @DisplayName("Deve usar builder sem id e gerar id nulo")
    void deveCriarSemId() {
        Empresa empresa = Empresa.builder()
                .codigoErp("ERP-002")
                .nome("Nova Empresa")
                .build();

        assertThat(empresa.getId()).isNull();
    }
}

