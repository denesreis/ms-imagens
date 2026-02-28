package com.scasistemas.msimagens.application.usecases;

import com.scasistemas.msimagens.application.dto.imagem.ImagemResponse;
import com.scasistemas.msimagens.application.dto.imagem.ImagemUploadRequest;
import com.scasistemas.msimagens.application.mappers.ImagemMapper;
import com.scasistemas.msimagens.domain.entities.AuditLog;
import com.scasistemas.msimagens.domain.entities.Imagem;
import com.scasistemas.msimagens.domain.exceptions.ResourceNotFoundException;
import com.scasistemas.msimagens.domain.repositories.IAuditLogRepository;
import com.scasistemas.msimagens.domain.repositories.IImagemRepository;
import com.scasistemas.msimagens.domain.repositories.IProdutoRepository;
import com.scasistemas.msimagens.domain.services.CloudflareUploadResult;
import com.scasistemas.msimagens.domain.services.ICloudflareImageService;
import com.scasistemas.msimagens.infrastructure.cloudflare.ImageCompressionService;
import com.scasistemas.msimagens.infrastructure.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Use Case para upload de imagem de produto.
 *
 * <p>
 * Ciclo de vida:
 * <ol>
 * <li>Registra imagem com status {@code PENDENTE} no banco</li>
 * <li>Comprime/redimensiona via Thumbnailator (opcional)</li>
 * <li>Faz upload à Cloudflare Images API</li>
 * <li>Atualiza status para {@code ATIVO} (sucesso) ou {@code ERRO} (falha)</li>
 * </ol>
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CreateImagemUseCase {

        private final IImagemRepository imagemRepository;
        private final IProdutoRepository produtoRepository;
        private final IAuditLogRepository auditLogRepository;
        private final ICloudflareImageService cloudflareImageService;
        private final ImageCompressionService imageCompressionService;
        private final ImagemMapper imagemMapper;

        @CacheEvict(value = "imagensPorEan", allEntries = true)
        @Transactional
        public ImagemResponse execute(MultipartFile file, ImagemUploadRequest request) throws IOException {
                log.info("[CreateImagem] Upload de imagem para produto id='{}'", request.getIdProduto());

                // Verificar se produto existe
                produtoRepository.findById(request.getIdProduto())
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Produto não encontrado: " + request.getIdProduto()));

                // Definir idEmpresa: USUARIO usa o do token
                Long idEmpresa = SecurityUtils.isAdministrador()
                                ? request.getIdEmpresa()
                                : SecurityUtils.getCurrentIdEmpresa();

                String filename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "imagem";
                String contentType = file.getContentType() != null ? file.getContentType() : "image/jpeg";

                // 1. Criar registro com status PENDENTE
                Imagem imagem = Imagem.builder()
                                .idProduto(request.getIdProduto())
                                .idEmpresa(idEmpresa)
                                .tipoArmazenamento(request.getTipoArmazenamento())
                                .filename(filename)
                                .build();

                Imagem pendente = imagemRepository.save(imagem);
                log.info("[CreateImagem] Registro PENDENTE criado id='{}'", pendente.getId());

                // 2. Comprimir imagem (fail-safe: retorna original em caso de falha)
                byte[] imageBytes = imageCompressionService.compress(file.getBytes(), contentType, filename);

                // 3. Upload para Cloudflare
                CloudflareUploadResult resultado = cloudflareImageService.uploadImage(imageBytes, filename,
                                contentType);

                // 4. Atualizar status baseado no resultado
                if (resultado.isSuccess()) {
                        pendente.confirmarUpload(resultado.getImageId(), resultado.getUrl());
                        log.info("[CreateImagem] Upload bem-sucedido cloudflareId='{}'", resultado.getImageId());
                } else {
                        pendente.marcarErro();
                        log.warn("[CreateImagem] Upload falhou para imagem id='{}': {}", pendente.getId(),
                                        resultado.getErrorMessage());
                }

                Imagem salva = imagemRepository.save(pendente);

                // 5. Audit log
                auditLogRepository.save(AuditLog.builder()
                                .acao("UPLOAD_IMAGEM")
                                .usuario(SecurityUtils.getCurrentUsername())
                                .idEmpresa(idEmpresa)
                                .detalhes("Imagem id=" + salva.getId() + " | produto=" + request.getIdProduto()
                                                + " | status=" + salva.getStatus())
                                .build());

                return imagemMapper.toResponse(salva);
        }

        /**
         * Variante do execute() que recebe bytes brutos em vez de
         * {@link MultipartFile}.
         * Usada pelo processamento em lote (batch) onde a imagem vem de uma URL
         * externa.
         *
         * @param bytes       bytes da imagem
         * @param filename    nome do arquivo (ex: "7891234567890.jpg")
         * @param contentType MIME type (ex: "image/jpeg")
         * @param request     metadados do produto e empresa
         */
        @CacheEvict(value = "imagensPorEan", allEntries = true)
        @Transactional
        public ImagemResponse executeFromBytes(byte[] bytes, String filename, String contentType,
                        ImagemUploadRequest request) {
                log.info("[CreateImagem] Upload (bytes) para produto id='{}'", request.getIdProduto());

                produtoRepository.findById(request.getIdProduto())
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Produto não encontrado: " + request.getIdProduto()));

                Long idEmpresa = SecurityUtils.isAdministrador()
                                ? request.getIdEmpresa()
                                : SecurityUtils.getCurrentIdEmpresa();

                // 1. Registro PENDENTE
                Imagem imagem = Imagem.builder()
                                .idProduto(request.getIdProduto())
                                .idEmpresa(idEmpresa)
                                .tipoArmazenamento(request.getTipoArmazenamento())
                                .filename(filename)
                                .build();
                Imagem pendente = imagemRepository.save(imagem);

                // 2. Compressão (fail-safe)
                byte[] compressed;
                try {
                        compressed = imageCompressionService.compress(bytes, contentType, filename);
                } catch (Exception e) {
                        log.warn("[CreateImagem] Falha na compressão, usando bytes originais: {}", e.getMessage());
                        compressed = bytes;
                }

                // 3. Upload Cloudflare
                CloudflareUploadResult resultado = cloudflareImageService.uploadImage(compressed, filename,
                                contentType);

                // 4. Atualizar status
                if (resultado.isSuccess()) {
                        pendente.confirmarUpload(resultado.getImageId(), resultado.getUrl());
                        log.info("[CreateImagem] Upload (bytes) bem-sucedido cloudflareId='{}'",
                                        resultado.getImageId());
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

                return imagemMapper.toResponse(salva);
        }
}
