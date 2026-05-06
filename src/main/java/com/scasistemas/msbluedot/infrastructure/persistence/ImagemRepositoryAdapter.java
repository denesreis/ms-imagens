package com.scasistemas.msbluedot.infrastructure.persistence;

import com.scasistemas.msbluedot.application.mappers.ImagemMapper;
import com.scasistemas.msbluedot.domain.entities.Imagem;
import com.scasistemas.msbluedot.domain.enums.StatusImagemEnum;
import com.scasistemas.msbluedot.domain.enums.TipoArmazenamentoEnum;
import com.scasistemas.msbluedot.domain.repositories.IImagemRepository;
import com.scasistemas.msbluedot.infrastructure.persistence.repositories.ImagemJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Adapter que implementa {@link IImagemRepository} usando JPA.
 */
@Component
@RequiredArgsConstructor
public class ImagemRepositoryAdapter implements IImagemRepository {

    private final ImagemJpaRepository jpaRepository;
    private final ImagemMapper mapper;

    @Override
    public Optional<Imagem> findById(String id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Imagem> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Imagem save(Imagem imagem) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(imagem)));
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
    public List<Imagem> findByIdProduto(String idProduto) {
        return jpaRepository.findByIdProduto(idProduto,
                org.springframework.data.domain.Pageable.unpaged())
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Imagem> findByCodigoEanAndTipoAberto(String codigoEan) {
        return jpaRepository.findByCodigoEanAndTipoAberto(codigoEan, StatusImagemEnum.ATIVO)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Imagem> findByIdProdutoAndTipoArmazenamento(
            String idProduto, TipoArmazenamentoEnum tipoArmazenamento) {
        return jpaRepository.findByIdProdutoAndTipoArmazenamento(idProduto, tipoArmazenamento)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}

