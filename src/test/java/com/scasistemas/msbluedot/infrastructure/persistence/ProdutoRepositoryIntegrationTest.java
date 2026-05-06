package com.scasistemas.msbluedot.infrastructure.persistence;

import com.scasistemas.msbluedot.JpaAuditingTestConfig;
import com.scasistemas.msbluedot.infrastructure.persistence.entities.ProdutoEntity;
import com.scasistemas.msbluedot.infrastructure.persistence.repositories.ProdutoJpaRepository;
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
@DisplayName("ProdutoJpaRepository - Testes de integração JPA")
class ProdutoRepositoryIntegrationTest {

    @Autowired
    private ProdutoJpaRepository produtoRepository;

    private ProdutoEntity buildProduto(String codigoEan, Long idEmpresa) {
        return ProdutoEntity.builder()
                .descricao("Produto Teste " + codigoEan)
                .codigoEan(codigoEan)
                .codigoErp("ERP-" + codigoEan)
                .idEmpresa(idEmpresa)
                .build();
    }

    @Test
    @DisplayName("Deve gerar UUID ao salvar produto")
    void deveGerarUuidAoSalvarProduto() {
        ProdutoEntity salvo = produtoRepository.save(buildProduto("7891234567890", 1L));

        assertThat(salvo.getId()).isNotNull();
        assertThat(salvo.getId()).isNotEmpty();
    }

    @Test
    @DisplayName("Deve buscar produto por ID")
    void deveBuscarProdutoPorId() {
        ProdutoEntity salvo = produtoRepository.save(buildProduto("7891234567891", 1L));

        Optional<ProdutoEntity> encontrado = produtoRepository.findById(salvo.getId());

        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getCodigoEan()).isEqualTo("7891234567891");
    }

    @Test
    @DisplayName("Deve buscar produtos por codigoEan")
    void deveBuscarProdutosPorCodigoEan() {
        produtoRepository.save(buildProduto("7891234567892", 1L));
        produtoRepository.save(buildProduto("7891234567892", 2L)); // mesmo EAN, empresa diferente

        List<ProdutoEntity> produtos = produtoRepository.findByCodigoEan("7891234567892");

        assertThat(produtos).hasSize(2);
    }

    @Test
    @DisplayName("Deve listar produtos de uma empresa com paginação")
    void deveListarProdutosPorEmpresaPaginado() {
        produtoRepository.save(buildProduto("1111111111111", 10L));
        produtoRepository.save(buildProduto("2222222222222", 10L));
        produtoRepository.save(buildProduto("3333333333333", 11L));

        Page<ProdutoEntity> pagina = produtoRepository.findByIdEmpresa(10L, PageRequest.of(0, 10));

        assertThat(pagina.getContent()).hasSize(2);
        assertThat(pagina.getContent()).allMatch(p -> p.getIdEmpresa().equals(10L));
    }

    @Test
    @DisplayName("Deve aplicar soft delete — produto não retorna nos selects")
    void deveAplicarSoftDeleteAoProduto() {
        ProdutoEntity salvo = produtoRepository.save(buildProduto("9999999999999", 1L));
        String id = salvo.getId();

        produtoRepository.deleteById(id);
        produtoRepository.flush();

        Optional<ProdutoEntity> aposDelete = produtoRepository.findById(id);
        assertThat(aposDelete).isEmpty();
    }

    @Test
    @DisplayName("findByCodigoEan não deve retornar produto com soft delete")
    void naoDeveRetornarProdutoDeleteadoPorCodigoEan() {
        ProdutoEntity salvo = produtoRepository.save(buildProduto("8888888888888", 1L));
        produtoRepository.deleteById(salvo.getId());
        produtoRepository.flush();

        List<ProdutoEntity> produtos = produtoRepository.findByCodigoEan("8888888888888");
        assertThat(produtos).isEmpty();
    }
}

