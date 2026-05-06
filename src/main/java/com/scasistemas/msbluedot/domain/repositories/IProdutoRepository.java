package com.scasistemas.msbluedot.domain.repositories;

import com.scasistemas.msbluedot.domain.entities.Produto;

import java.util.List;
import java.util.Optional;

/**
 * Contrato de repositório para a entidade {@link Produto}.
 *
 * <p>
 * Interface de domínio puro — sem dependência de frameworks.
 * A implementação está na camada de infraestrutura.
 * </p>
 */
public interface IProdutoRepository {

    /**
     * Busca um produto pelo ID (UUID).
     * Retorna apenas produtos com {@code ativo = true}.
     */
    Optional<Produto> findById(String id);

    /**
     * Lista todos os produtos ativos.
     * Aplica soft delete: ignora registros com {@code ativo = false}.
     */
    List<Produto> findAll();

    /** Persiste ou atualiza um produto. */
    Produto save(Produto produto);

    /**
     * Soft delete: marca {@code ativo = false} sem excluir o registro.
     *
     * @param id UUID do produto a ser desativado
     */
    void deleteById(String id);

    /** Verifica se existe um produto ativo com o ID informado. */
    boolean existsById(String id);

    /**
     * Busca um produto pelo código EAN-13.
     * Retorna apenas produtos com {@code ativo = true}.
     *
     * @param codigoEan código de barras EAN-13
     */
    Optional<Produto> findByCodigoEan(String codigoEan);

    /**
     * Verifica se já existe um produto ativo com o EAN informado para a empresa.
     * Permite o mesmo EAN em empresas diferentes.
     */
    boolean existsByCodigoEanAndIdEmpresa(String codigoEan, Long idEmpresa);

    /**
     * Verifica se já existe um produto ativo com o EAN informado sem empresa associada (produto global).
     */
    boolean existsByCodigoEanAndIdEmpresaIsNull(String codigoEan);

    /**
     * Lista todos os produtos ativos de uma empresa.
     *
     * @param idEmpresa ID da empresa
     */
    List<Produto> findByIdEmpresa(Long idEmpresa);
}

