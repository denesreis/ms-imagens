package com.scasistemas.msbluedot.infrastructure.persistence;

import com.scasistemas.msbluedot.application.mappers.EmpresaMapper;
import com.scasistemas.msbluedot.domain.entities.Empresa;
import com.scasistemas.msbluedot.domain.repositories.IEmpresaRepository;
import com.scasistemas.msbluedot.infrastructure.persistence.repositories.EmpresaJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Adapter que implementa {@link IEmpresaRepository} usando JPA.
 */
@Component
@RequiredArgsConstructor
public class EmpresaRepositoryAdapter implements IEmpresaRepository {

    private final EmpresaJpaRepository jpaRepository;
    private final EmpresaMapper mapper;

    @Override
    public Optional<Empresa> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Empresa> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Empresa save(Empresa empresa) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(empresa)));
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
    public Optional<Empresa> findByCodigoErp(String codigoErp) {
        return jpaRepository.findByCodigoErp(codigoErp).map(mapper::toDomain);
    }
}

