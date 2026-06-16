package com.scasistemas.msbluedot.service;

import com.scasistemas.msbluedot.cloudflare.CloudflareImageClient;
import com.scasistemas.msbluedot.cloudflare.CloudflareUploadResult;
import com.scasistemas.msbluedot.cloudflare.ImageCompressionService;
import com.scasistemas.msbluedot.config.CacheConfig;
import com.scasistemas.msbluedot.dto.ImagemProdutoResponse;
import com.scasistemas.msbluedot.dto.ImagemResponse;
import com.scasistemas.msbluedot.dto.ImagemUploadRequest;
import com.scasistemas.msbluedot.entity.AuditLog;
import com.scasistemas.msbluedot.entity.Imagem;
import com.scasistemas.msbluedot.enums.StatusImagemEnum;
import com.scasistemas.msbluedot.enums.TipoArmazenamentoEnum;
import com.scasistemas.msbluedot.exception.ResourceNotFoundException;
import com.scasistemas.msbluedot.exception.UnauthorizedException;
import com.scasistemas.msbluedot.repository.AuditLogRepository;
import com.scasistemas.msbluedot.repository.ImagemRepository;
import com.scasistemas.msbluedot.repository.ProdutoRepository;
import com.scasistemas.msbluedot.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImagemService {

    private final ImagemRepository imagemRepository;
    private final ProdutoRepository produtoRepository;
    private final AuditLogRepository auditLogRepository;
    private final CloudflareImageClient cloudflareImageClient;
    private final ImageCompressionService imageCompressionService;

    @CacheEvict(value = CacheConfig.CACHE_IMAGENS_POR_EAN, allEntries = true)
    @Transactional
    public ImagemResponse upload(MultipartFile file, ImagemUploadRequest request) throws IOException {
        log.info("[CreateImagem] Upload de imagem para produto id='{}'", request.getIdProduto());

        produtoRepository.findById(request.getIdProduto())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Produto não encontrado: " + request.getIdProduto()));

        Long idEmpresa = SecurityUtils.isAdministrador()
                ? request.getIdEmpresa()
                : SecurityUtils.getCurrentIdEmpresa();

        String filename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "imagem";
        String contentType = file.getContentType() != null ? file.getContentType() : "image/jpeg";

        Imagem pendente = imagemRepository.save(Imagem.builder()
                .idProduto(request.getIdProduto())
                .idEmpresa(idEmpresa)
                .tipoArmazenamento(request.getTipoArmazenamento())
                .filename(filename)
                .build());

        log.info("[CreateImagem] Registro PENDENTE criado id='{}'", pendente.getId());

        byte[] imageBytes = imageCompressionService.compress(file.getBytes(), contentType, filename);
        CloudflareUploadResult resultado = cloudflareImageClient.uploadImage(imageBytes, filename, contentType);

        if (resultado.isSuccess()) {
            pendente.confirmarUpload(resultado.getImageId(), resultado.getUrl());
            log.info("[CreateImagem] Upload bem-sucedido cloudflareId='{}'", resultado.getImageId());
        } else {
            pendente.marcarErro();
            log.warn("[CreateImagem] Upload falhou para imagem id='{}': {}", pendente.getId(),
                    resultado.getErrorMessage());
        }

        Imagem salva = imagemRepository.save(pendente);

        auditLogRepository.save(AuditLog.builder()
                .acao("UPLOAD_IMAGEM")
                .usuario(SecurityUtils.getCurrentUsername())
                .idEmpresa(idEmpresa)
                .detalhes("Imagem id=" + salva.getId() + " | produto=" + request.getIdProduto()
                        + " | status=" + salva.getStatus())
                .build());

        return toResponse(salva);
    }

    @CacheEvict(value = CacheConfig.CACHE_IMAGENS_POR_EAN, allEntries = true)
    @Transactional
    public ImagemResponse uploadFromBytes(byte[] bytes, String filename, String contentType,
                                          ImagemUploadRequest request) {
        log.info("[CreateImagem] Upload (bytes) para produto id='{}'", request.getIdProduto());

        produtoRepository.findById(request.getIdProduto())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Produto não encontrado: " + request.getIdProduto()));

        Long idEmpresa = SecurityUtils.isAdministrador()
                ? request.getIdEmpresa()
                : SecurityUtils.getCurrentIdEmpresa();

        Imagem pendente = imagemRepository.save(Imagem.builder()
                .idProduto(request.getIdProduto())
                .idEmpresa(idEmpresa)
                .tipoArmazenamento(request.getTipoArmazenamento())
                .filename(filename)
                .build());

        byte[] compressed;
        try {
            compressed = imageCompressionService.compress(bytes, contentType, filename);
        } catch (Exception e) {
            log.warn("[CreateImagem] Falha na compressão, usando bytes originais: {}", e.getMessage());
            compressed = bytes;
        }

        CloudflareUploadResult resultado = cloudflareImageClient.uploadImage(compressed, filename, contentType);

        if (resultado.isSuccess()) {
            pendente.confirmarUpload(resultado.getImageId(), resultado.getUrl());
            log.info("[CreateImagem] Upload (bytes) bem-sucedido cloudflareId='{}'", resultado.getImageId());
        } else {
            pendente.marcarErro();
            log.warn("[CreateImagem] Upload (bytes) falhou id='{}': {}", pendente.getId(),
                    resultado.getErrorMessage());
        }

        Imagem salva = imagemRepository.save(pendente);

        auditLogRepository.save(AuditLog.builder()
                .acao("UPLOAD_IMAGEM_BATCH")
                .usuario(SecurityUtils.getCurrentUsername())
                .idEmpresa(idEmpresa)
                .detalhes("Imagem id=" + salva.getId() + " | produto=" + request.getIdProduto()
                        + " | status=" + salva.getStatus() + " | origem=eanpictures")
                .build());

        return toResponse(salva);
    }

    @Transactional(readOnly = true)
    public ImagemResponse buscarPorId(String id) {
        log.debug("[GetImagemById] Buscando imagem id={}", id);

        Imagem imagem = imagemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Imagem não encontrada: " + id));

        if (!SecurityUtils.isAdministrador()) {
            Long idEmpresaToken = SecurityUtils.getCurrentIdEmpresa();
            if (imagem.getIdEmpresa() != null && !imagem.getIdEmpresa().equals(idEmpresaToken)) {
                throw new UnauthorizedException("Acesso negado à imagem solicitada");
            }
        }

        return toResponse(imagem);
    }

    @Cacheable(value = CacheConfig.CACHE_IMAGENS_POR_EAN, key = "#codigoEan")
    @Transactional(readOnly = true)
    public List<ImagemProdutoResponse> buscarPorEan(String codigoEan) {
        log.debug("[GetImagensByEan] Buscando imagens para ean='{}'", codigoEan);

        List<Imagem> imagens = imagemRepository.findByCodigoEanAndTipoAberto(codigoEan, StatusImagemEnum.ATIVO);

        List<ImagemProdutoResponse> response = imagens.stream()
                .map(this::toProdutoResponse)
                .toList();

        log.debug("[GetImagensByEan] {} imagens encontradas para ean='{}'", response.size(), codigoEan);
        return response;
    }

    @Transactional(readOnly = true)
    public Page<ImagemResponse> buscarPorProduto(String idProduto, Pageable pageable) {
        log.debug("[GetImagensByProduto] Buscando imagens do produto id='{}'", idProduto);

        var produto = produtoRepository.findById(idProduto)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado: " + idProduto));

        List<Imagem> imagens = filtrarImagensPorProduto(idProduto, produto);

        List<ImagemResponse> responses = imagens.stream().map(this::toResponse).toList();

        int total = responses.size();
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), total);
        List<ImagemResponse> paginado = (start >= total) ? List.of() : responses.subList(start, end);
        return new PageImpl<>(paginado, pageable, total);
    }

    @Transactional(readOnly = true)
    public List<ImagemResponse> buscarTodosPorProduto(String idProduto) {
        log.debug("[GetImagensByProduto] Buscando todas as imagens do produto id='{}'", idProduto);

        var produto = produtoRepository.findById(idProduto)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado: " + idProduto));

        return filtrarImagensPorProduto(idProduto, produto).stream()
                .map(this::toResponse)
                .toList();
    }

    @CacheEvict(value = CacheConfig.CACHE_IMAGENS_POR_EAN, allEntries = true)
    @Transactional
    public ImagemResponse atualizar(String id, TipoArmazenamentoEnum tipoArmazenamento) {
        log.info("[UpdateImagem] Atualizando imagem id={} tipo={}", id, tipoArmazenamento);

        Imagem imagem = imagemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Imagem não encontrada: " + id));

        if (!SecurityUtils.isAdministrador()) {
            Long idEmpresaToken = SecurityUtils.getCurrentIdEmpresa();
            if (imagem.getIdEmpresa() != null && !imagem.getIdEmpresa().equals(idEmpresaToken)) {
                throw new UnauthorizedException("Acesso negado à imagem solicitada");
            }
        }

        imagem.setTipoArmazenamento(tipoArmazenamento);
        Imagem atualizada = imagemRepository.save(imagem);

        log.info("[UpdateImagem] Imagem id={} atualizada para tipo={}", id, tipoArmazenamento);
        return toResponse(atualizada);
    }

    @CacheEvict(value = CacheConfig.CACHE_IMAGENS_POR_EAN, allEntries = true)
    @Transactional
    public void deletar(String id) {
        log.info("[DeleteImagem] Deletando imagem id={}", id);

        Imagem imagem = imagemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Imagem não encontrada: " + id));

        if (!SecurityUtils.isAdministrador()) {
            Long idEmpresaToken = SecurityUtils.getCurrentIdEmpresa();
            if (imagem.getIdEmpresa() != null && !imagem.getIdEmpresa().equals(idEmpresaToken)) {
                throw new UnauthorizedException("Acesso negado à imagem solicitada");
            }
        }

        if (StringUtils.hasText(imagem.getIdImagemCloudflare())) {
            try {
                cloudflareImageClient.deleteImage(imagem.getIdImagemCloudflare());
                log.info("[DeleteImagem] Imagem removida da Cloudflare: {}", imagem.getIdImagemCloudflare());
            } catch (Exception ex) {
                log.warn("[DeleteImagem] Falha ao remover da Cloudflare (ignorando): {}", ex.getMessage());
            }
        }

        imagemRepository.deleteById(id);

        auditLogRepository.save(AuditLog.builder()
                .acao("DELETAR_IMAGEM")
                .usuario(SecurityUtils.getCurrentUsername())
                .idEmpresa(imagem.getIdEmpresa())
                .detalhes("Imagem deletada: " + id + " | produto=" + imagem.getIdProduto())
                .build());

        log.info("[DeleteImagem] Imagem id={} deletada com sucesso", id);
    }

    public ImagemResponse toResponse(Imagem i) {
        return ImagemResponse.builder()
                .id(i.getId())
                .idProduto(i.getIdProduto())
                .idEmpresa(i.getIdEmpresa())
                .tipoArmazenamento(i.getTipoArmazenamento())
                .idImagemCloudflare(i.getIdImagemCloudflare())
                .url(i.getUrl())
                .filename(i.getFilename())
                .status(i.getStatus())
                .ativo(i.getAtivo())
                .dataCriacao(i.getDataCriacao())
                .dataAtualizacao(i.getDataAtualizacao())
                .build();
    }

    public ImagemProdutoResponse toProdutoResponse(Imagem i) {
        return ImagemProdutoResponse.builder()
                .id(i.getId())
                .url(i.getUrl())
                .filename(i.getFilename())
                .tipoArmazenamento(i.getTipoArmazenamento())
                .status(i.getStatus())
                .build();
    }

    private List<Imagem> filtrarImagensPorProduto(String idProduto,
                                                    com.scasistemas.msbluedot.entity.Produto produto) {
        if (SecurityUtils.isAdministrador()) {
            return imagemRepository.findByIdProdutoAndTipoArmazenamento(idProduto, TipoArmazenamentoEnum.ABERTO);
        } else {
            Long idEmpresaToken = SecurityUtils.getCurrentIdEmpresa();
            if (produto.getIdEmpresa() != null && !produto.getIdEmpresa().equals(idEmpresaToken)) {
                throw new UnauthorizedException("Acesso negado ao produto solicitado");
            }
            return imagemRepository.findByIdProdutoAndTipoArmazenamento(idProduto, TipoArmazenamentoEnum.PRIVADO);
        }
    }
}
