package com.scasistemas.msimagens.infrastructure.persistence;

import com.scasistemas.msimagens.application.mappers.ProdutoMapper;
import com.scasistemas.msimagens.domain.entities.Produto;
import com.scasistemas.msimagens.domain.repositories.IProdutoRepository;
import com.scasistemas.msimagens.infrastructure.persistence.repositories.ProdutoJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Adapter que implementa {@link IProdutoRepository} usando JPA.
 */
@Component
@RequiredArgsConstructor
public class ProdutoRepositoryAdapter implements IProdutoRepository {

    private final ProdutoJpaRepository jpaRepository;
    private final ProdutoMapper mapper;

    @Override
    public Optional<Produto> findById(String id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Produto> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Produto save(Produto produto) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(produto)));
    }

    @Override
    public void deleteById(String id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsById(String id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public Optional<Produto> findByCodigoEan(String codigoEan) {
        return jpaRepository.findByCodigoEan(codigoEan).stream().findFirst()
                .map(mapper::toDomain);
    }

    @Override
    public boolean existsByCodigoEanAndIdEmpresa(String codigoEan, Long idEmpresa) {
        return jpaRepository.existsByCodigoEanAndIdEmpresa(codigoEan, idEmpresa);
    }

    @Override
    public boolean existsByCodigoEanAndIdEmpresaIsNull(String codigoEan) {
        return jpaRepository.existsByCodigoEanAndIdEmpresaIsNull(codigoEan);
    }

    @Override
    public List<Produto> findByIdEmpresa(Long idEmpresa) {
        return jpaRepository.findByIdEmpresa(idEmpresa,
                org.springframework.data.domain.Pageable.unpaged())
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}
