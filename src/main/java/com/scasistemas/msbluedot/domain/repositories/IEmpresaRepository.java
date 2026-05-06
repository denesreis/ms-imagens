package com.scasistemas.msbluedot.domain.repositories;

import com.scasistemas.msbluedot.domain.entities.Empresa;

import java.util.List;
import java.util.Optional;

/**
 * Contrato de repositório para a entidade {@link Empresa}.
 *
 * <p>
 * Interface de domínio puro — sem dependência de frameworks.
 * A implementação está na camada de infraestrutura.
 * </p>
 */
public interface IEmpresaRepository {

    /**
     * Busca uma empresa pelo ID.
     * Retorna apenas empresas com {@code ativo = true}.
     */
    Optional<Empresa> findById(Long id);

    /**
     * Lista todas as empresas ativas.
     * Aplica soft delete: ignora registros com {@code ativo = false}.
     */
    List<Empresa> findAll();

    /** Persiste ou atualiza uma empresa. */
    Empresa save(Empresa empresa);

    /**
     * Soft delete: marca {@code ativo = false} sem excluir o registro.
     *
     * @param id ID da empresa a ser desativada
     */
    void deleteById(Long id);

    /** Verifica se existe uma empresa ativa com o ID informado. */
    boolean existsById(Long id);

    /**
     * Busca uma empresa pelo código ERP.
     * Retorna apenas empresas com {@code ativo = true}.
     *
     * @param codigoErp código de integração com o ERP
     */
    Optional<Empresa> findByCodigoErp(String codigoErp);
}

