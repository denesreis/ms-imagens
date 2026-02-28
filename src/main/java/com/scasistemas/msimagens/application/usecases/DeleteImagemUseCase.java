package com.scasistemas.msimagens.application.usecases;

import com.scasistemas.msimagens.domain.entities.AuditLog;
import com.scasistemas.msimagens.domain.entities.Imagem;
import com.scasistemas.msimagens.domain.exceptions.ResourceNotFoundException;
import com.scasistemas.msimagens.domain.exceptions.UnauthorizedException;
import com.scasistemas.msimagens.domain.repositories.IAuditLogRepository;
import com.scasistemas.msimagens.domain.repositories.IImagemRepository;
import com.scasistemas.msimagens.domain.services.ICloudflareImageService;
import com.scasistemas.msimagens.infrastructure.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Use Case para exclusão de imagem.
 *
 * <p>
 * Fluxo:
 * <ol>
 * <li>Verifica existência e permissão de empresa</li>
 * <li>Tenta deletar da Cloudflare (falha não impede o soft delete local)</li>
 * <li>Soft delete no banco ({@code ativo = false} via @SQLDelete)</li>
 * <li>Invalida cache {@code imagensPorEan}</li>
 * <li>Grava audit log</li>
 * </ol>
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeleteImagemUseCase {

    private final IImagemRepository imagemRepository;
    private final IAuditLogRepository auditLogRepository;
    private final ICloudflareImageService cloudflareImageService;

    @CacheEvict(value = "imagensPorEan", allEntries = true)
    @Transactional
    public void execute(String id) {
        log.info("[DeleteImagem] Deletando imagem id={}", id);

        Imagem imagem = imagemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Imagem não encontrada: " + id));

        // Verificar permissão de empresa para não-admins
        if (!SecurityUtils.isAdministrador()) {
            Long idEmpresaToken = SecurityUtils.getCurrentIdEmpresa();
            if (imagem.getIdEmpresa() != null && !imagem.getIdEmpresa().equals(idEmpresaToken)) {
                throw new UnauthorizedException("Acesso negado à imagem solicitada");
            }
        }

        // Tentar deletar da Cloudflare (fail-safe: erro não cancela o soft delete
        // local)
        if (StringUtils.hasText(imagem.getIdImagemCloudflare())) {
            try {
                cloudflareImageService.deleteImage(imagem.getIdImagemCloudflare());
                log.info("[DeleteImagem] Imagem removida da Cloudflare: {}", imagem.getIdImagemCloudflare());
            } catch (Exception ex) {
                log.warn("[DeleteImagem] Falha ao remover da Cloudflare (ignorando): {}", ex.getMessage());
            }
        }

        // Soft delete no banco
        imagemRepository.deleteById(id);

        auditLogRepository.save(AuditLog.builder()
                .acao("DELETAR_IMAGEM")
                .usuario(SecurityUtils.getCurrentUsername())
                .idEmpresa(imagem.getIdEmpresa())
                .detalhes("Imagem deletada: " + id + " | produto=" + imagem.getIdProduto())
                .build());

        log.info("[DeleteImagem] Imagem id={} deletada com sucesso", id);
    }
}
