package com.scasistemas.msimagens.application.usecases;

import com.scasistemas.msimagens.application.dto.produto.ProdutoRequest;
import com.scasistemas.msimagens.application.dto.produto.ProdutoResponse;
import com.scasistemas.msimagens.application.mappers.ProdutoMapper;
import com.scasistemas.msimagens.domain.entities.AuditLog;
import com.scasistemas.msimagens.domain.entities.Imagem;
import com.scasistemas.msimagens.domain.entities.Produto;
import com.scasistemas.msimagens.domain.enums.StatusImagemEnum;
import com.scasistemas.msimagens.domain.enums.TipoArmazenamentoEnum;
import com.scasistemas.msimagens.domain.exceptions.BusinessException;
import com.scasistemas.msimagens.domain.repositories.IAuditLogRepository;
import com.scasistemas.msimagens.domain.repositories.IImagemRepository;
import com.scasistemas.msimagens.domain.repositories.IProdutoRepository;
import com.scasistemas.msimagens.infrastructure.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Use Case para criação de produto.
 * ADMIN pode criar em qualquer empresa; USUARIO herda o idEmpresa do token.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CreateProdutoUseCase {

    private final IProdutoRepository produtoRepository;
    private final IImagemRepository imagemRepository;
    private final IAuditLogRepository auditLogRepository;
    private final ProdutoMapper produtoMapper;

    @Transactional
    public ProdutoResponse execute(ProdutoRequest request) {
        log.info("[CreateProduto] Criando produto ean='{}'", request.getCodigoEan());

        // Definir idEmpresa: USUARIO usa o do token; ADMIN pode informar explicitamente
        Long idEmpresa;
        if (SecurityUtils.isAdministrador()) {
            idEmpresa = request.getIdEmpresa(); // pode ser null (produto compartilhado)
        } else {
            idEmpresa = SecurityUtils.getCurrentIdEmpresa();
        }

        // Verificar duplicidade de codigoEan dentro da mesma empresa (EAN pode repetir entre empresas diferentes)
        if (request.getCodigoEan() != null) {
            boolean duplicado = idEmpresa != null
                    ? produtoRepository.existsByCodigoEanAndIdEmpresa(request.getCodigoEan(), idEmpresa)
                    : produtoRepository.existsByCodigoEanAndIdEmpresaIsNull(request.getCodigoEan());
            if (duplicado) {
                throw new BusinessException("Já existe um produto com o EAN: " + request.getCodigoEan()
                        + " para esta empresa.");
            }
        }

        Produto produto = produtoMapper.fromRequest(request);
        produto.setIdEmpresa(idEmpresa);

        Produto salvo = produtoRepository.save(produto);

        // Herdar imagens ABERTO existentes de outros produtos com o mesmo EAN
        int imagensHerdadas = 0;
        if (salvo.getCodigoEan() != null) {
            List<Imagem> imagensExistentes = imagemRepository.findByCodigoEanAndTipoAberto(salvo.getCodigoEan());
            for (Imagem origem : imagensExistentes) {
                Imagem copia = Imagem.builder()
                        // id omitido: o @UuidGenerator da entidade gera automaticamente
                        .idProduto(salvo.getId())
                        .idEmpresa(idEmpresa)
                        .tipoArmazenamento(TipoArmazenamentoEnum.ABERTO)
                        .idImagemCloudflare(origem.getIdImagemCloudflare())
                        .url(origem.getUrl())
                        .filename(origem.getFilename())
                        .status(StatusImagemEnum.ATIVO)
                        .ativo(true)
                        .build();
                imagemRepository.save(copia);
                imagensHerdadas++;
            }
            if (imagensHerdadas > 0) {
                log.info("[CreateProduto] {} imagem(ns) herdada(s) do EAN {} para produto id={}",
                        imagensHerdadas, salvo.getCodigoEan(), salvo.getId());
            }
        }

        auditLogRepository.save(AuditLog.builder()
                .acao("CRIAR_PRODUTO")
                .usuario(SecurityUtils.getCurrentUsername())
                .idEmpresa(idEmpresa)
                .detalhes("Produto criado: " + salvo.getId() + " | ean=" + salvo.getCodigoEan()
                        + " | imagensHerdadas=" + imagensHerdadas)
                .build());

        log.info("[CreateProduto] Produto id={} criado com sucesso", salvo.getId());
        return produtoMapper.toResponse(salvo);
    }
}
