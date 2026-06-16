package com.scasistemas.msbluedot.repository;

import com.scasistemas.msbluedot.entity.Imagem;
import com.scasistemas.msbluedot.enums.StatusImagemEnum;
import com.scasistemas.msbluedot.enums.TipoArmazenamentoEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImagemRepository extends JpaRepository<Imagem, String> {

    List<Imagem> findByIdProduto(String idProduto);

    List<Imagem> findByIdProdutoAndTipoArmazenamento(String idProduto, TipoArmazenamentoEnum tipoArmazenamento);

    @Query("""
            SELECT i FROM Imagem i
            JOIN Produto p ON p.id = i.idProduto
            WHERE p.codigoEan = :codigoEan
              AND i.tipoArmazenamento = 'ABERTO'
              AND i.status = :status
            """)
    List<Imagem> findByCodigoEanAndTipoAberto(
            @Param("codigoEan") String codigoEan,
            @Param("status") StatusImagemEnum status);

    @Query("SELECT DISTINCT i.idProduto FROM Imagem i WHERE i.tipoArmazenamento = :tipo")
    List<String> findDistinctIdProdutosByTipoArmazenamento(@Param("tipo") TipoArmazenamentoEnum tipo);
}
