package com.scasistemas.msimagens.application.usecases;

import com.scasistemas.msimagens.application.dto.imagem.ImagemResponse;
import com.scasistemas.msimagens.application.mappers.ImagemMapper;
import com.scasistemas.msimagens.domain.entities.Imagem;
import com.scasistemas.msimagens.domain.exceptions.ResourceNotFoundException;
import com.scasistemas.msimagens.domain.exceptions.UnauthorizedException;
import com.scasistemas.msimagens.domain.repositories.IImagemRepository;
import com.scasistemas.msimagens.infrastructure.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use Case para busca de imagem por ID.
 * Verifica permissão: imagens ABERTO são públicas; PRIVADO exige mesma empresa
 * ou ADMIN.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GetImagemByIdUseCase {

    private final IImagemRepository imagemRepository;
    private final ImagemMapper imagemMapper;

    @Transactional(readOnly = true)
    public ImagemResponse execute(String id) {
        log.debug("[GetImagemById] Buscando imagem id={}", id);

        Imagem imagem = imagemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Imagem não encontrada: " + id));

        // Verificar permissão: imagem PRIVADA requer mesma empresa ou ADMIN
        if (!SecurityUtils.isAdministrador()) {
            Long idEmpresaToken = SecurityUtils.getCurrentIdEmpresa();
            if (imagem.getIdEmpresa() != null && !imagem.getIdEmpresa().equals(idEmpresaToken)) {
                throw new UnauthorizedException("Acesso negado à imagem solicitada");
            }
        }

        return imagemMapper.toResponse(imagem);
    }
}
