package com.scasistemas.msbluedot.domain.entities;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Produto - Testes de domínio")
class ProdutoTest {

    @Test
    @DisplayName("Deve criar produto com ativo=true por padrão")
    void deveCriarComAtivoTrue() {
        Produto produto = Produto.builder()
                .descricao("Caneta Azul")
                .codigoEan("7891234567890")
                .idEmpresa(1L)
                .build();

        assertThat(produto.getAtivo()).isTrue();
        assertThat(produto.getDescricao()).isEqualTo("Caneta Azul");
        assertThat(produto.getCodigoEan()).isEqualTo("7891234567890");
    }

    @Test
    @DisplayName("Deve permitir soft delete marcando ativo=false")
    void deveSoftDelete() {
        Produto produto = Produto.builder()
                .descricao("Produto Teste")
                .build();

        produto.setAtivo(false);

        assertThat(produto.getAtivo()).isFalse();
    }

    @Test
    @DisplayName("Deve construir produto com UUID")
    void deveConstruirComUuid() {
        String uuid = "550e8400-e29b-41d4-a716-446655440000";
        Produto produto = Produto.builder()
                .id(uuid)
                .descricao("Produto com UUID")
                .build();

        assertThat(produto.getId()).isEqualTo(uuid);
    }

    @Test
    @DisplayName("Deve aceitar campos opcionais nulos")
    void deveAceitarCamposOpcionaisNulos() {
        Produto produto = Produto.builder()
                .descricao("Produto Mínimo")
                .build();

        assertThat(produto.getDiscriminacao()).isNull();
        assertThat(produto.getCodigoErp()).isNull();
        assertThat(produto.getCodigoEan()).isNull();
    }
}

