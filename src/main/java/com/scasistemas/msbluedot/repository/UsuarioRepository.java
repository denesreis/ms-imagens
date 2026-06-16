package com.scasistemas.msbluedot.repository;

import com.scasistemas.msbluedot.entity.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByNomeIgnoreCase(String nome);

    Page<Usuario> findByIdEmpresa(Long idEmpresa, Pageable pageable);

    @Query(value = "SELECT COUNT(*) > 0 FROM tb_usuario WHERE nome = :nome", nativeQuery = true)
    boolean existsByNomeIgnoringSoftDelete(@Param("nome") String nome);

    @Query(value = "SELECT * FROM tb_usuario WHERE LOWER(nome) = LOWER(:nome) LIMIT 1", nativeQuery = true)
    Optional<Usuario> findByNomeIgnoringSoftDelete(@Param("nome") String nome);
}
