package com.scasistemas.msbluedot.application.usecases;

import com.scasistemas.msbluedot.application.dto.imagem.ImagemResponse;
import com.scasistemas.msbluedot.application.mappers.ImagemMapper;
import com.scasistemas.msbluedot.domain.entities.Imagem;
import com.scasistemas.msbluedot.domain.exceptions.ResourceNotFoundException;
import com.scasistemas.msbluedot.domain.exceptions.UnauthorizedException;
import com.scasistemas.msbluedot.domain.repositories.IImagemRepository;
import com.scasistemas.msbluedot.domain.repositories.IProdutoRepository;
import com.scasistemas.msbluedot.infrastructure.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Use Case para listagem de imagens de um produto com paginação.
 * Verifica permissão de empresa para usuários não-admin.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GetImagensByProdutoUseCase {

    private final IImagemRepository imagemRepository;
    private final IProdutoRepository produtoRepository;
    private final ImagemMapper imagemMapper;

    @Transactional(readOnly = true)
    public Page<ImagemResponse> execute(String idProduto, Pageable pageable) {
        log.debug("[GetImagensByProduto] Buscando imagens do produto id='{}'", idProduto);

        // Verificar se produto existe
        var produto = produtoRepository.findById(idProduto)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado: " + idProduto));

        // Verificar permissão de empresa para USUARIOS
        if (!SecurityUtils.isAdministrador()) {
            Long idEmpresaToken = SecurityUtils.getCurrentIdEmpresa();
            if (produto.getIdEmpresa() != null && !produto.getIdEmpresa().equals(idEmpresaToken)) {
                throw new UnauthorizedException("Acesso negado ao produto solicitado");
            }
        }

        List<Imagem> imagens = imagemRepository.findByIdProduto(idProduto);
        List<ImagemResponse> responses = imagens.stream()
                .map(imagemMapper::toResponse)
                .toList();

        int total = responses.size();
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), total);

        List<ImagemResponse> paginado = (start >= total) ? List.of() : responses.subList(start, end);
        return new PageImpl<>(paginado, pageable, total);
    }

    /**
     * Retorna todas as imagens de um produto sem paginação.
     * Inclui a mesma verificação de permissão de empresa.
     *
     * @param idProduto UUID do produto
     * @return lista completa de respostas de imagem
     */
    @Transactional(readOnly = true)
    public List<ImagemResponse> executeAll(String idProduto) {
        log.debug("[GetImagensByProduto] Buscando todas as imagens do produto id='{}'", idProduto);

        var produto = produtoRepository.findById(idProduto)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado: " + idProduto));

        if (!SecurityUtils.isAdministrador()) {
            Long idEmpresaToken = SecurityUtils.getCurrentIdEmpresa();
            if (produto.getIdEmpresa() != null && !produto.getIdEmpresa().equals(idEmpresaToken)) {
                throw new UnauthorizedException("Acesso negado ao produto solicitado");
            }
        }

        return imagemRepository.findByIdProduto(idProduto).stream()
                .map(imagemMapper::toResponse)
                .toList();
    }
}

