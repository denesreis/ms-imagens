package com.scasistemas.msbluedot.service;

import com.scasistemas.msbluedot.dto.BatchProdutoItemResult;
import com.scasistemas.msbluedot.dto.BatchProdutoResponse;
import com.scasistemas.msbluedot.dto.ImagemResponse;
import com.scasistemas.msbluedot.dto.ImagemUploadRequest;
import com.scasistemas.msbluedot.dto.ProdutoRequest;
import com.scasistemas.msbluedot.dto.ProdutoResponse;
import com.scasistemas.msbluedot.eanpictures.EanPicturesResult;
import com.scasistemas.msbluedot.eanpictures.EanPicturesService;
import com.scasistemas.msbluedot.entity.AuditLog;
import com.scasistemas.msbluedot.entity.Imagem;
import com.scasistemas.msbluedot.entity.Produto;
import com.scasistemas.msbluedot.enums.StatusImagemEnum;
import com.scasistemas.msbluedot.enums.TipoArmazenamentoEnum;
import com.scasistemas.msbluedot.exception.BusinessException;
import com.scasistemas.msbluedot.exception.ResourceNotFoundException;
import com.scasistemas.msbluedot.exception.UnauthorizedException;
import com.scasistemas.msbluedot.repository.AuditLogRepository;
import com.scasistemas.msbluedot.repository.ImagemRepository;
import com.scasistemas.msbluedot.repository.ProdutoRepository;
import com.scasistemas.msbluedot.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final ImagemRepository imagemRepository;
    private final AuditLogRepository auditLogRepository;
    private final ImagemService imagemService;
    private final EanPicturesService eanPicturesService;

    @Transactional
    public ProdutoResponse criar(ProdutoRequest request) {
        log.info("[CreateProduto] Criando produto ean='{}'", request.getCodigoEan());

        Long idEmpresa = resolverIdEmpresa(request.getIdEmpresa());

        if (request.getCodigoEan() != null) {
            boolean duplicado = idEmpresa != null
                    ? produtoRepository.existsByCodigoEanAndIdEmpresa(request.getCodigoEan(), idEmpresa)
                    : produtoRepository.existsByCodigoEanAndIdEmpresaIsNull(request.getCodigoEan());
            if (duplicado) {
                throw new BusinessException("Já existe um produto com o EAN: " + request.getCodigoEan()
                        + " para esta empresa.");
            }
        }

        Produto produto = Produto.builder()
                .descricao(request.getDescricao())
                .discriminacao(request.getDiscriminacao())
                .codigoErp(request.getCodigoErp())
                .codigoEan(request.getCodigoEan())
                .idEmpresa(idEmpresa)
                .ativo(true)
                .build();

        Produto salvo = produtoRepository.save(produto);

        int imagensHerdadas = herdarImagensAberto(salvo, idEmpresa);
        if (imagensHerdadas > 0) {
            log.info("[CreateProduto] {} imagem(ns) herdada(s) do EAN {} para produto id={}",
                    imagensHerdadas, salvo.getCodigoEan(), salvo.getId());
        }

        auditLogRepository.save(AuditLog.builder()
                .acao("CRIAR_PRODUTO")
                .usuario(SecurityUtils.getCurrentUsername())
                .idEmpresa(idEmpresa)
                .detalhes("Produto criado: " + salvo.getId() + " | ean=" + salvo.getCodigoEan()
                        + " | imagensHerdadas=" + imagensHerdadas)
                .build());

        log.info("[CreateProduto] Produto id={} criado com sucesso", salvo.getId());
        return toResponse(salvo);
    }

    @Transactional
    public ProdutoResponse atualizar(String id, ProdutoRequest request) {
        log.info("[UpdateProduto] Atualizando produto id={}", id);

        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado: " + id));

        verificarPermissao(produto);

        produto.setDescricao(request.getDescricao());
        produto.setDiscriminacao(request.getDiscriminacao());
        produto.setCodigoErp(request.getCodigoErp());
        produto.setCodigoEan(request.getCodigoEan());
        if (SecurityUtils.isAdministrador() && request.getIdEmpresa() != null) {
            produto.setIdEmpresa(request.getIdEmpresa());
        }

        Produto atualizado = produtoRepository.save(produto);

        auditLogRepository.save(AuditLog.builder()
                .acao("ATUALIZAR_PRODUTO")
                .usuario(SecurityUtils.getCurrentUsername())
                .idEmpresa(atualizado.getIdEmpresa())
                .detalhes("Produto atualizado: " + atualizado.getId())
                .build());

        log.info("[UpdateProduto] Produto id={} atualizado com sucesso", id);
        return toResponse(atualizado);
    }

    @Transactional(readOnly = true)
    public ProdutoResponse buscarPorId(String id) {
        log.debug("[GetProduto] Buscando produto id={}", id);

        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado: " + id));

        if (!SecurityUtils.isAdministrador()) {
            Long idEmpresaToken = SecurityUtils.getCurrentIdEmpresa();
            if (produto.getIdEmpresa() != null && !produto.getIdEmpresa().equals(idEmpresaToken)) {
                throw new UnauthorizedException("Acesso negado ao produto solicitado");
            }
        }

        return toResponse(produto);
    }

    @Transactional(readOnly = true)
    public Page<ProdutoResponse> listar(Pageable pageable) {
        log.debug("[ListProdutos] Listando produtos page={} size={}", pageable.getPageNumber(), pageable.getPageSize());

        List<Produto> todos;
        if (SecurityUtils.isAdministrador()) {
            List<String> ids = imagemRepository.findDistinctIdProdutosByTipoArmazenamento(TipoArmazenamentoEnum.ABERTO);
            todos = ids.stream()
                    .map(produtoRepository::findById)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .toList();
        } else {
            Long idEmpresa = SecurityUtils.getCurrentIdEmpresa();
            todos = produtoRepository.findByIdEmpresa(idEmpresa);
        }

        List<ProdutoResponse> responses = todos.stream().map(this::toResponse).toList();

        int total = responses.size();
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), total);
        List<ProdutoResponse> paginado = (start >= total) ? List.of() : responses.subList(start, end);
        return new PageImpl<>(paginado, pageable, total);
    }

    @Transactional
    public void deletar(String id) {
        log.info("[DeleteProduto] Soft delete produto id={}", id);

        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado: " + id));

        if (!SecurityUtils.isAdministrador()) {
            Long idEmpresaToken = SecurityUtils.getCurrentIdEmpresa();
            if (produto.getIdEmpresa() != null && !produto.getIdEmpresa().equals(idEmpresaToken)) {
                throw new UnauthorizedException("Acesso negado ao produto solicitado");
            }
        }

        produtoRepository.deleteById(id);

        auditLogRepository.save(AuditLog.builder()
                .acao("DELETAR_PRODUTO")
                .usuario(SecurityUtils.getCurrentUsername())
                .idEmpresa(produto.getIdEmpresa())
                .detalhes("Produto desativado: " + produto.getId() + " | ean=" + produto.getCodigoEan())
                .build());

        log.info("[DeleteProduto] Produto id={} desativado com sucesso", id);
    }

    public BatchProdutoResponse criarLote(List<ProdutoRequest> produtos) {
        log.info("[BatchCreateProduto] Iniciando lote com {} produto(s)", produtos.size());

        List<BatchProdutoItemResult> resultados = new ArrayList<>();
        int criados = 0, erros = 0, ignorados = 0, semImagem = 0;

        for (ProdutoRequest request : produtos) {
            BatchProdutoItemResult resultado = processarItemLote(request);
            resultados.add(resultado);

            switch (resultado.getStatus()) {
                case "CRIADO" -> criados++;
                case "IGNORADO" -> ignorados++;
                case "SEM_IMAGEM" -> semImagem++;
                default -> erros++;
            }
        }

        log.info("[BatchCreateProduto] Lote finalizado: criados={} semImagem={} erros={} ignorados={}",
                criados, semImagem, erros, ignorados);

        return BatchProdutoResponse.builder()
                .total(produtos.size())
                .criados(criados)
                .erros(erros)
                .ignorados(ignorados)
                .semImagem(semImagem)
                .resultados(resultados)
                .build();
    }

    public ProdutoResponse toResponse(Produto p) {
        return ProdutoResponse.builder()
                .id(p.getId())
                .descricao(p.getDescricao())
                .discriminacao(p.getDiscriminacao())
                .codigoErp(p.getCodigoErp())
                .codigoEan(p.getCodigoEan())
                .idEmpresa(p.getIdEmpresa())
                .ativo(p.getAtivo())
                .dataCriacao(p.getDataCriacao())
                .dataAtualizacao(p.getDataAtualizacao())
                .build();
    }

    private BatchProdutoItemResult processarItemLote(ProdutoRequest request) {
        String ean = request.getCodigoEan();
        log.debug("[BatchCreateProduto] Processando EAN={}", ean);

        Optional<EanPicturesResult> imagemOpt = Optional.empty();
        boolean imagemEncontrada = false;

        if (ean != null && !ean.isBlank()) {
            imagemOpt = eanPicturesService.fetchImage(ean);
            imagemEncontrada = imagemOpt.isPresent();
        }

        if (imagemOpt.isEmpty()) {
            log.info("[BatchCreateProduto] EAN={} sem imagem no eanpictures — produto não cadastrado", ean);
            return BatchProdutoItemResult.builder()
                    .codigoEan(ean)
                    .status("SEM_IMAGEM")
                    .mensagem("Imagem não encontrada em eanpictures.com.br — produto não foi cadastrado")
                    .imagemEncontrada(false)
                    .imagemCarregada(false)
                    .build();
        }

        ProdutoResponse produtoCriado;
        try {
            produtoCriado = criar(request);
        } catch (BusinessException ex) {
            log.info("[BatchCreateProduto] EAN={} ignorado: {}", ean, ex.getMessage());
            return BatchProdutoItemResult.builder()
                    .codigoEan(ean)
                    .status("IGNORADO")
                    .mensagem(ex.getMessage())
                    .imagemEncontrada(imagemEncontrada)
                    .imagemCarregada(false)
                    .build();
        } catch (Exception ex) {
            log.error("[BatchCreateProduto] EAN={} erro inesperado: {}", ean, ex.getMessage());
            return BatchProdutoItemResult.builder()
                    .codigoEan(ean)
                    .status("ERRO")
                    .mensagem("Erro ao criar produto: " + ex.getMessage())
                    .imagemEncontrada(imagemEncontrada)
                    .imagemCarregada(false)
                    .build();
        }

        boolean imagemCarregada = false;
        EanPicturesResult img = imagemOpt.get();
        try {
            ImagemUploadRequest uploadRequest = ImagemUploadRequest.builder()
                    .idProduto(produtoCriado.getId())
                    .idEmpresa(produtoCriado.getIdEmpresa())
                    .tipoArmazenamento(TipoArmazenamentoEnum.ABERTO)
                    .build();
            ImagemResponse imagemResponse = imagemService.uploadFromBytes(
                    img.bytes(), img.filename(), img.contentType(), uploadRequest);
            produtoCriado.setImagens(List.of(imagemResponse));
            imagemCarregada = true;
            log.info("[BatchCreateProduto] EAN={} imagem carregada para Cloudflare", ean);
        } catch (Exception ex) {
            log.warn("[BatchCreateProduto] EAN={} produto criado mas upload falhou: {}", ean, ex.getMessage());
        }

        String mensagem = imagemCarregada
                ? "Produto criado com imagem da eanpictures.com.br"
                : "Produto criado, mas falha no upload da imagem para a Cloudflare";

        return BatchProdutoItemResult.builder()
                .codigoEan(ean)
                .status("CRIADO")
                .mensagem(mensagem)
                .imagemEncontrada(imagemEncontrada)
                .imagemCarregada(imagemCarregada)
                .produto(produtoCriado)
                .build();
    }

    private Long resolverIdEmpresa(Long idEmpresaRequest) {
        if (SecurityUtils.isAdministrador()) {
            return idEmpresaRequest;
        }
        return SecurityUtils.getCurrentIdEmpresa();
    }

    private void verificarPermissao(Produto produto) {
        if (!SecurityUtils.isAdministrador()) {
            Long idEmpresaToken = SecurityUtils.getCurrentIdEmpresa();
            if (produto.getIdEmpresa() != null && !produto.getIdEmpresa().equals(idEmpresaToken)) {
                throw new UnauthorizedException("Acesso negado ao produto solicitado");
            }
        }
    }

    private int herdarImagensAberto(Produto salvo, Long idEmpresa) {
        if (salvo.getCodigoEan() == null) return 0;

        List<Imagem> imagensExistentes = imagemRepository
                .findByCodigoEanAndTipoAberto(salvo.getCodigoEan(), StatusImagemEnum.ATIVO);
        int count = 0;
        for (Imagem origem : imagensExistentes) {
            Imagem copia = Imagem.builder()
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
            count++;
        }
        return count;
    }
}
