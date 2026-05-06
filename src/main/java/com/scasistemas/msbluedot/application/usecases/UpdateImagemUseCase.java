package com.scasistemas.msbluedot.application.usecases;

import com.scasistemas.msbluedot.application.dto.imagem.ImagemResponse;
import com.scasistemas.msbluedot.application.mappers.ImagemMapper;
import com.scasistemas.msbluedot.domain.entities.Imagem;
import com.scasistemas.msbluedot.domain.enums.TipoArmazenamentoEnum;
import com.scasistemas.msbluedot.domain.exceptions.ResourceNotFoundException;
import com.scasistemas.msbluedot.domain.exceptions.UnauthorizedException;
import com.scasistemas.msbluedot.domain.repositories.IImagemRepository;
import com.scasistemas.msbluedot.infrastructure.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use Case para atualização do tipo de armazenamento de uma imagem.
 * Verifica permissão de empresa. Invalida cache de imagens por EAN.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateImagemUseCase {

    private final IImagemRepository imagemRepository;
    private final ImagemMapper imagemMapper;

    @CacheEvict(value = "imagensPorEan", allEntries = true)
    @Transactional
    public ImagemResponse execute(String id, TipoArmazenamentoEnum tipoArmazenamento) {
        log.info("[UpdateImagem] Atualizando imagem id={} tipo={}", id, tipoArmazenamento);

        Imagem imagem = imagemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Imagem não encontrada: " + id));

        // Verificar permissão de empresa para não-admins
        if (!SecurityUtils.isAdministrador()) {
            Long idEmpresaToken = SecurityUtils.getCurrentIdEmpresa();
            if (imagem.getIdEmpresa() != null && !imagem.getIdEmpresa().equals(idEmpresaToken)) {
                throw new UnauthorizedException("Acesso negado à imagem solicitada");
            }
        }

        imagem.setTipoArmazenamento(tipoArmazenamento);
        Imagem atualizada = imagemRepository.save(imagem);

        log.info("[UpdateImagem] Imagem id={} atualizada para tipo={}", id, tipoArmazenamento);
        return imagemMapper.toResponse(atualizada);
    }
}

