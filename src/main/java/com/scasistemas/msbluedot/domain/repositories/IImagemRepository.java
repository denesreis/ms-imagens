package com.scasistemas.msbluedot.domain.repositories;

import com.scasistemas.msbluedot.domain.entities.Imagem;
import com.scasistemas.msbluedot.domain.enums.TipoArmazenamentoEnum;

import java.util.List;
import java.util.Optional;

/**
 * Contrato de repositório para a entidade {@link Imagem}.
 *
 * <p>
 * Interface de domínio puro — sem dependência de frameworks.
 * A implementação está na camada de infraestrutura.
 * </p>
 */
public interface IImagemRepository {

    /**
     * Busca uma imagem pelo ID (UUID).
     * Retorna apenas imagens com {@code ativo = true}.
     */
    Optional<Imagem> findById(String id);

    /**
     * Lista todas as imagens ativas.
     * Aplica soft delete: ignora registros com {@code ativo = false}.
     */
    List<Imagem> findAll();

    /** Persiste ou atualiza uma imagem. */
    Imagem save(Imagem imagem);

    /**
     * Soft delete: marca {@code ativo = false} sem excluir o registro.
     *
     * @param id UUID da imagem a ser desativada
     */
    void deleteById(String id);

    /** Verifica se existe uma imagem ativa com o ID informado. */
    boolean existsById(String id);

    /**
     * Lista todas as imagens ativas de um produto.
     *
     * @param idProduto UUID do produto
     */
    List<Imagem> findByIdProduto(String idProduto);

    /**
     * Busca imagens públicas de um produto pelo código EAN.
     * Usado pelo endpoint público GET /imagens/ean/{codigoEan} (sem JWT).
     * Retorna apenas imagens com {@code tipoArmazenamento = ABERTO},
     * {@code status = ATIVO} e {@code ativo = true}.
     *
     * @param codigoEan código de barras EAN-13 do produto
     */
    List<Imagem> findByCodigoEanAndTipoAberto(String codigoEan);

    /**
     * Lista imagens ativas de um produto filtradas por tipo de armazenamento.
     *
     * @param idProduto         UUID do produto
     * @param tipoArmazenamento tipo de acesso da imagem
     */
    List<Imagem> findByIdProdutoAndTipoArmazenamento(String idProduto, TipoArmazenamentoEnum tipoArmazenamento);

    /**
     * Retorna IDs distintos de produtos que possuem ao menos uma imagem ativa
     * com o tipo de armazenamento informado.
     *
     * @param tipoArmazenamento ABERTO ou PRIVADO
     */
    List<String> findDistinctIdProdutosByTipoArmazenamento(TipoArmazenamentoEnum tipoArmazenamento);
}
