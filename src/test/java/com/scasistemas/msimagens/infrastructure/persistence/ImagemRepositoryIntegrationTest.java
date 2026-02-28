package com.scasistemas.msimagens.infrastructure.persistence;

import com.scasistemas.msimagens.JpaAuditingTestConfig;
import com.scasistemas.msimagens.domain.enums.StatusImagemEnum;
import com.scasistemas.msimagens.domain.enums.TipoArmazenamentoEnum;
import com.scasistemas.msimagens.infrastructure.persistence.entities.ImagemEntity;
import com.scasistemas.msimagens.infrastructure.persistence.entities.ProdutoEntity;
import com.scasistemas.msimagens.infrastructure.persistence.repositories.ImagemJpaRepository;
import com.scasistemas.msimagens.infrastructure.persistence.repositories.ProdutoJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import(JpaAuditingTestConfig.class)
@DisplayName("ImagemJpaRepository - Testes de integração JPA")
class ImagemRepositoryIntegrationTest {

    @Autowired
    private ImagemJpaRepository imagemRepository;

    @Autowired
    private ProdutoJpaRepository produtoRepository;

    private ProdutoEntity produto;
    private static final String CODIGO_EAN = "7891234500001";

    @BeforeEach
    void setUp() {
        produto = produtoRepository.save(ProdutoEntity.builder()
                .descricao("Produto para Imagem")
                .codigoEan(CODIGO_EAN)
                .idEmpresa(1L)
                .build());
    }

    private ImagemEntity buildImagem(String idProduto, TipoArmazenamentoEnum tipo, StatusImagemEnum status) {
        return ImagemEntity.builder()
                .idProduto(idProduto)
                .idEmpresa(1L)
                .tipoArmazenamento(tipo)
                .filename("foto.jpg")
                .status(status)
                .build();
    }

    @Test
    @DisplayName("Deve gerar UUID ao salvar imagem")
    void deveGerarUuidAoSalvarImagem() {
        ImagemEntity salva = imagemRepository.save(
                buildImagem(produto.getId(), TipoArmazenamentoEnum.ABERTO, StatusImagemEnum.ATIVO));

        assertThat(salva.getId()).isNotNull().isNotEmpty();
    }

    @Test
    @DisplayName("Deve buscar imagem por ID")
    void deveBuscarImagemPorId() {
        ImagemEntity salva = imagemRepository.save(
                buildImagem(produto.getId(), TipoArmazenamentoEnum.ABERTO, StatusImagemEnum.ATIVO));

        Optional<ImagemEntity> encontrada = imagemRepository.findById(salva.getId());

        assertThat(encontrada).isPresent();
        assertThat(encontrada.get().getStatus()).isEqualTo(StatusImagemEnum.ATIVO);
    }

    @Test
    @DisplayName("Deve listar imagens de um produto por página")
    void deveListarImagensPorProdutoPaginado() {
        imagemRepository.save(buildImagem(produto.getId(), TipoArmazenamentoEnum.ABERTO, StatusImagemEnum.ATIVO));
        imagemRepository.save(buildImagem(produto.getId(), TipoArmazenamentoEnum.PRIVADO, StatusImagemEnum.ATIVO));

        Page<ImagemEntity> pagina = imagemRepository.findByIdProduto(produto.getId(), PageRequest.of(0, 10));

        assertThat(pagina.getContent()).hasSize(2);
    }

    @Test
    @DisplayName("findByCodigoEanAndTipoAberto deve retornar apenas imagens ABERTO+ATIVO")
    void deveBuscarImagensPublicasPorCodigoEan() {
        imagemRepository.save(buildImagem(produto.getId(), TipoArmazenamentoEnum.ABERTO, StatusImagemEnum.ATIVO));
        imagemRepository.save(buildImagem(produto.getId(), TipoArmazenamentoEnum.ABERTO, StatusImagemEnum.PENDENTE));
        imagemRepository.save(buildImagem(produto.getId(), TipoArmazenamentoEnum.PRIVADO, StatusImagemEnum.ATIVO));

        List<ImagemEntity> resultado = imagemRepository.findByCodigoEanAndTipoAberto(
                CODIGO_EAN, StatusImagemEnum.ATIVO);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getTipoArmazenamento()).isEqualTo(TipoArmazenamentoEnum.ABERTO);
        assertThat(resultado.get(0).getStatus()).isEqualTo(StatusImagemEnum.ATIVO);
    }

    @Test
    @DisplayName("findByCodigoEanAndTipoAberto deve retornar lista vazia para EAN sem imagens ABERTO+ATIVO")
    void deveRetornarVazioQuandoNaoHaImagensPublicasAtivas() {
        imagemRepository.save(buildImagem(produto.getId(), TipoArmazenamentoEnum.PRIVADO, StatusImagemEnum.ATIVO));

        List<ImagemEntity> resultado = imagemRepository.findByCodigoEanAndTipoAberto(
                CODIGO_EAN, StatusImagemEnum.ATIVO);

        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("Deve aplicar soft delete — imagem não retorna nos selects")
    void deveAplicarSoftDeleteAoImagem() {
        ImagemEntity salva = imagemRepository.save(
                buildImagem(produto.getId(), TipoArmazenamentoEnum.ABERTO, StatusImagemEnum.ATIVO));
        String id = salva.getId();

        imagemRepository.deleteById(id);
        imagemRepository.flush();

        Optional<ImagemEntity> aposDelete = imagemRepository.findById(id);
        assertThat(aposDelete).isEmpty();
    }

    @Test
    @DisplayName("findByCodigoEanAndTipoAberto não deve retornar imagem com soft delete")
    void naoDeveRetornarImagemDeleteadaNaBuscaPublica() {
        ImagemEntity salva = imagemRepository.save(
                buildImagem(produto.getId(), TipoArmazenamentoEnum.ABERTO, StatusImagemEnum.ATIVO));
        imagemRepository.deleteById(salva.getId());
        imagemRepository.flush();

        List<ImagemEntity> resultado = imagemRepository.findByCodigoEanAndTipoAberto(
                CODIGO_EAN, StatusImagemEnum.ATIVO);

        assertThat(resultado).isEmpty();
    }
}
