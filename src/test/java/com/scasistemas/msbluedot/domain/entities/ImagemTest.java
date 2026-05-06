package com.scasistemas.msbluedot.domain.entities;

import com.scasistemas.msbluedot.domain.enums.StatusImagemEnum;
import com.scasistemas.msbluedot.domain.enums.TipoArmazenamentoEnum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Imagem - Testes de domínio")
class ImagemTest {

    private Imagem buildImagem() {
        return Imagem.builder()
                .idProduto("prod-uuid-001")
                .idEmpresa(1L)
                .tipoArmazenamento(TipoArmazenamentoEnum.ABERTO)
                .filename("foto.jpg")
                .build();
    }

    @Test
    @DisplayName("Deve criar imagem com status PENDENTE por padrão")
    void deveCriarComStatusPendente() {
        Imagem imagem = buildImagem();

        assertThat(imagem.getStatus()).isEqualTo(StatusImagemEnum.PENDENTE);
        assertThat(imagem.getAtivo()).isTrue();
        assertThat(imagem.getIdImagemCloudflare()).isNull();
        assertThat(imagem.getUrl()).isNull();
    }

    @Test
    @DisplayName("Deve transicionar para ATIVO após confirmarUpload")
    void deveTransicionarParaAtivoAposConfirmarUpload() {
        Imagem imagem = buildImagem();

        imagem.confirmarUpload("cloudflare-img-id-123", "https://imagedelivery.net/test/cloudflare-img-id-123/public");

        assertThat(imagem.getStatus()).isEqualTo(StatusImagemEnum.ATIVO);
        assertThat(imagem.getIdImagemCloudflare()).isEqualTo("cloudflare-img-id-123");
        assertThat(imagem.getUrl()).isEqualTo("https://imagedelivery.net/test/cloudflare-img-id-123/public");
    }

    @Test
    @DisplayName("Deve transicionar para ERRO ao marcarErro")
    void deveTransicionarParaErroAoMarcarErro() {
        Imagem imagem = buildImagem();

        imagem.marcarErro();

        assertThat(imagem.getStatus()).isEqualTo(StatusImagemEnum.ERRO);
        assertThat(imagem.getIdImagemCloudflare()).isNull();
    }

    @Test
    @DisplayName("Deve retornar estaDisponivel=true quando ATIVO e ativo=true")
    void deveRetornarDisponivelQuandoAtivo() {
        Imagem imagem = buildImagem();
        imagem.confirmarUpload("cf-id", "https://url.test");

        assertThat(imagem.estaDisponivel()).isTrue();
    }

    @Test
    @DisplayName("Deve retornar estaDisponivel=false quando status PENDENTE")
    void naoDeveRetornarDisponivelQuandoPendente() {
        Imagem imagem = buildImagem();
        assertThat(imagem.estaDisponivel()).isFalse();
    }

    @Test
    @DisplayName("Deve retornar estaDisponivel=false quando status ERRO")
    void naoDeveRetornarDisponivelQuandoErro() {
        Imagem imagem = buildImagem();
        imagem.marcarErro();
        assertThat(imagem.estaDisponivel()).isFalse();
    }

    @Test
    @DisplayName("Deve retornar estaDisponivel=false quando ativo=false mesmo com status ATIVO")
    void naoDeveRetornarDisponivelQuandoInativo() {
        Imagem imagem = buildImagem();
        imagem.confirmarUpload("cf-id", "https://url.test");
        imagem.setAtivo(false);

        assertThat(imagem.estaDisponivel()).isFalse();
    }

    @Test
    @DisplayName("Deve suportar tipo PRIVADO")
    void deveSuportarTipoPrivado() {
        Imagem imagem = Imagem.builder()
                .idProduto("prod-002")
                .tipoArmazenamento(TipoArmazenamentoEnum.PRIVADO)
                .build();

        assertThat(imagem.getTipoArmazenamento()).isEqualTo(TipoArmazenamentoEnum.PRIVADO);
    }
}

