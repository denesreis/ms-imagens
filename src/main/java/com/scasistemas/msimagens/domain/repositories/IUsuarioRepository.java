package com.scasistemas.msimagens.domain.repositories;

import com.scasistemas.msimagens.domain.entities.Usuario;

import java.util.List;
import java.util.Optional;

/**
 * Contrato de repositório para a entidade {@link Usuario}.
 *
 * <p>
 * Interface de domínio puro — sem dependência de frameworks.
 * A implementação está na camada de infraestrutura.
 * </p>
 */
public interface IUsuarioRepository {

    /**
     * Busca um usuário pelo ID.
     * Retorna apenas usuários com {@code ativo = true}.
     */
    Optional<Usuario> findById(Long id);

    /**
     * Lista todos os usuários ativos.
     * Aplica soft delete: ignora registros com {@code ativo = false}.
     */
    List<Usuario> findAll();

    /** Persiste ou atualiza um usuário. */
    Usuario save(Usuario usuario);

    /**
     * Soft delete: marca {@code ativo = false} sem excluir o registro.
     *
     * @param id ID do usuário a ser desativado
     */
    void deleteById(Long id);

    /** Verifica se existe um usuário ativo com o ID informado. */
    boolean existsById(Long id);

    /**
     * Busca um usuário pelo nome (username para login).
     * Retorna apenas usuários com {@code ativo = true}.
     *
     * @param nome nome único do usuário
     */
    Optional<Usuario> findByNome(String nome);

    /**
     * Lista todos os usuários ativos de uma empresa.
     *
     * @param idEmpresa ID da empresa
     */
    List<Usuario> findByIdEmpresa(Long idEmpresa);

    /**
     * Verifica se já existe um usuário com o nome informado (independente do
     * ativo).
     * Usado para validar a constraint UNIQUE antes de salvar.
     *
     * @param nome nome a verificar
     */
    boolean existsByNome(String nome);
}
