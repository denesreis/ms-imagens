package com.scasistemas.msimagens.infrastructure.persistence;

import com.scasistemas.msimagens.application.mappers.UsuarioMapper;
import com.scasistemas.msimagens.domain.entities.Usuario;
import com.scasistemas.msimagens.domain.repositories.IUsuarioRepository;
import com.scasistemas.msimagens.infrastructure.persistence.repositories.UsuarioJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Adapter que implementa {@link IUsuarioRepository} usando JPA.
 *
 * <p>
 * Converte entre entidades de domínio e JPA usando {@link UsuarioMapper}.
 * </p>
 */
@Component
@RequiredArgsConstructor
public class UsuarioRepositoryAdapter implements IUsuarioRepository {

    private final UsuarioJpaRepository jpaRepository;
    private final UsuarioMapper mapper;

    @Override
    public Optional<Usuario> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Usuario> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Usuario save(Usuario usuario) {
        var entity = mapper.toEntity(usuario);
        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public Optional<Usuario> findByNome(String nome) {
        return jpaRepository.findByNomeIgnoreCase(nome).map(mapper::toDomain);
    }

    @Override
    public List<Usuario> findByIdEmpresa(Long idEmpresa) {
        return jpaRepository.findByIdEmpresa(idEmpresa,
                org.springframework.data.domain.Pageable.unpaged())
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsByNome(String nome) {
        return jpaRepository.existsByNomeIgnoringSoftDelete(nome);
    }
}
