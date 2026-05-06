package com.scasistemas.msbluedot.infrastructure.persistence.repositories;

import com.scasistemas.msbluedot.domain.enums.StatusImagemEnum;
import com.scasistemas.msbluedot.domain.enums.TipoArmazenamentoEnum;
import com.scasistemas.msbluedot.infrastructure.persistence.entities.ImagemEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repositório JPA para {@link ImagemEntity}.
 *
 * <p>
 * Os métodos de busca aplicam automaticamente a restrição de soft delete
 * via {@code @SQLRestriction("ativo = true")} na entidade.
 * </p>
 */
public interface ImagemJpaRepository extends JpaRepository<ImagemEntity, String> {

    /**
     * Lista imagens ativas de um produto com paginação.
     */
    Page<ImagemEntity> findByIdProduto(String idProduto, Pageable pageable);

    /**
     * Lista imagens ativas de um produto por tipo de armazenamento.
     */
    List<ImagemEntity> findByIdProdutoAndTipoArmazenamento(
            String idProduto, TipoArmazenamentoEnum tipoArmazenamento);

    /**
     * Busca imagens públicas (ABERTO + ATIVO) de um produto pelo código EAN.
     * Utilizado pelo endpoint público GET /imagens/ean/{codigoEan} (sem JWT).
     *
     * <p>
     * JOIN com tb_produto para resolver o codigoEan.
     * </p>
     */
    @Query("""
            SELECT i FROM ImagemEntity i
            JOIN ProdutoEntity p ON p.id = i.idProduto
            WHERE p.codigoEan = :codigoEan
              AND i.tipoArmazenamento = 'ABERTO'
              AND i.status = :status
            """)
    List<ImagemEntity> findByCodigoEanAndTipoAberto(
            @Param("codigoEan") String codigoEan,
            @Param("status") StatusImagemEnum status);
}

