package com.scasistemas.msbluedot.infrastructure.web.controllers;

import com.scasistemas.msbluedot.application.dto.imagem.ImagemResponse;
import com.scasistemas.msbluedot.application.dto.imagem.ImagemUploadRequest;
import com.scasistemas.msbluedot.application.dto.produto.BatchProdutoResponse;
import com.scasistemas.msbluedot.application.dto.produto.ProdutoRequest;
import com.scasistemas.msbluedot.application.dto.produto.ProdutoResponse;
import com.scasistemas.msbluedot.application.usecases.BatchCreateProdutoUseCase;
import com.scasistemas.msbluedot.application.usecases.CreateImagemUseCase;
import com.scasistemas.msbluedot.application.usecases.CreateProdutoUseCase;
import com.scasistemas.msbluedot.application.usecases.DeleteProdutoUseCase;
import com.scasistemas.msbluedot.application.usecases.GetImagensByProdutoUseCase;
import com.scasistemas.msbluedot.application.usecases.GetProdutoUseCase;
import com.scasistemas.msbluedot.application.usecases.ListProdutosUseCase;
import com.scasistemas.msbluedot.application.usecases.UpdateProdutoUseCase;
import com.scasistemas.msbluedot.domain.enums.TipoArmazenamentoEnum;
import com.scasistemas.msbluedot.infrastructure.security.annotation.IsUsuario;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/produtos")
@RequiredArgsConstructor
@Tag(name = "Produtos", description = "CRUD de produtos com upload de imagens")
public class ProdutoController {

    private final CreateProdutoUseCase createProdutoUseCase;
    private final UpdateProdutoUseCase updateProdutoUseCase;
    private final GetProdutoUseCase getProdutoUseCase;
    private final ListProdutosUseCase listProdutosUseCase;
    private final DeleteProdutoUseCase deleteProdutoUseCase;
    private final CreateImagemUseCase createImagemUseCase;
    private final GetImagensByProdutoUseCase getImagensByProdutoUseCase;
    private final BatchCreateProdutoUseCase batchCreateProdutoUseCase;

    /**
     * Cria um produto e, opcionalmente, faz upload das imagens vinculadas.
     *
     * <p>
     * Envio multipart: campo {@code produto} (JSON) + {@code imagens} (arquivos,
     * opcional)
     * + {@code tipoArmazenamento} ({@code ABERTO} por padrão quando não informado).
     * </p>
     *
     * <p>
     * As imagens são enviadas para a Cloudflare Images API e suas URLs ficam
     * registradas na resposta dentro do campo {@code imagens}.
     * </p>
     */
    /**
     * Cria um produto via JSON puro (sem imagem).
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @IsUsuario
    @Operation(summary = "Criar produto (JSON, sem imagem)", description = "Envio application/json. Para incluir imagens, use multipart/form-data.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Produto criado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "409", description = "Código EAN já cadastrado")
    })
    public ResponseEntity<ProdutoResponse> criarJson(
            @org.springframework.web.bind.annotation.RequestBody @Valid ProdutoRequest produtoRequest) {
        log.info("Criando produto (JSON) codigoEan={}", produtoRequest.getCodigoEan());
        ProdutoResponse produto = createProdutoUseCase.execute(produtoRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(produto);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @IsUsuario
    @Operation(summary = "Criar produto com upload de imagens (opcional)", description = "Envio multipart/form-data. Campo 'produto' = JSON do produto. "
            + "Campo 'imagens' = arquivos (opcional). "
            + "Param 'tipoArmazenamento' = ABERTO (padrão) ou PRIVADO.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Produto criado (imagens na resposta quando enviadas)"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "409", description = "Código EAN já cadastrado")
    })
    public ResponseEntity<ProdutoResponse> criar(
            @RequestPart("produto") @Valid ProdutoRequest produtoRequest,
            @RequestPart(name = "imagens", required = false) List<MultipartFile> imagens,
            @RequestParam(name = "tipoArmazenamento", required = false) TipoArmazenamentoEnum tipoArmazenamento)
            throws IOException {

        // Padrão: ABERTO quando não informado
        TipoArmazenamentoEnum tipo = tipoArmazenamento != null ? tipoArmazenamento : TipoArmazenamentoEnum.ABERTO;

        log.info("Criando produto codigoEan={} imagens={} tipoArmazenamento={}",
                produtoRequest.getCodigoEan(),
                imagens != null ? imagens.size() : 0,
                tipo);

        ProdutoResponse produto = createProdutoUseCase.execute(produtoRequest);

        if (imagens != null && !imagens.isEmpty()) {
            List<ImagemUploadRequest> uploadRequests = buildUploadRequests(
                    produto.getId(), produto.getIdEmpresa(), tipo, imagens.size());
            List<ImagemResponse> imagensResponse = new ArrayList<>();
            for (int i = 0; i < imagens.size(); i++) {
                try {
                    imagensResponse.add(createImagemUseCase.execute(imagens.get(i), uploadRequests.get(i)));
                } catch (Exception e) {
                    log.warn("Falha ao fazer upload da imagem {}: {}", i, e.getMessage());
                }
            }
            produto.setImagens(imagensResponse);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(produto);
    }

    @GetMapping("/{id}")
    @IsUsuario
    @Operation(summary = "Buscar produto por ID", description = "Retorna o produto com a lista de imagens associadas e suas URLs da Cloudflare.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Produto encontrado"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado")
    })
    public ResponseEntity<ProdutoResponse> buscarPorId(@PathVariable String id) {
        log.info("Buscando produto id={}", id);
        ProdutoResponse produto = getProdutoUseCase.execute(id);

        // Incluir imagens associadas (com URLs da Cloudflare) na resposta
        try {
            List<ImagemResponse> imagens = getImagensByProdutoUseCase.executeAll(id);
            if (!imagens.isEmpty()) {
                produto.setImagens(imagens);
            }
        } catch (Exception e) {
            log.warn("[ProdutoController] Erro ao buscar imagens do produto {}: {}", id, e.getMessage());
        }

        return ResponseEntity.ok(produto);
    }

    @GetMapping
    @IsUsuario
    @Operation(summary = "Listar produtos")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista paginada de produtos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    public ResponseEntity<Page<ProdutoResponse>> listar(
            @PageableDefault(size = 20, sort = "descricao") Pageable pageable) {
        log.info("Listando produtos page={} size={}", pageable.getPageNumber(), pageable.getPageSize());
        return ResponseEntity.ok(listProdutosUseCase.execute(pageable));
    }

    /**
     * Atualiza os dados de um produto e, opcionalmente, faz upload de novas
     * imagens.
     *
     * <p>
     * Envio multipart: campo {@code produto} (JSON) + {@code imagens} (arquivos,
     * opcional)
     * + {@code tipoArmazenamento} ({@code ABERTO} por padrão).
     * As imagens existentes <b>não são removidas</b>; as novas são adicionadas.
     * </p>
     */
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @IsUsuario
    @Operation(summary = "Atualizar produto com upload de imagens (opcional)", description = "Envio multipart/form-data. Campo 'produto' = JSON com dados a atualizar. "
            + "Campo 'imagens' = novos arquivos (opcional). "
            + "Param 'tipoArmazenamento' = ABERTO (padrão) ou PRIVADO. "
            + "As imagens existentes são mantidas; as novas são adicionadas.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Produto atualizado (novas imagens na resposta quando enviadas)"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado")
    })
    public ResponseEntity<ProdutoResponse> atualizar(
            @PathVariable String id,
            @RequestPart("produto") @Valid ProdutoRequest request,
            @RequestPart(name = "imagens", required = false) List<MultipartFile> imagens,
            @RequestParam(name = "tipoArmazenamento", required = false) TipoArmazenamentoEnum tipoArmazenamento)
            throws IOException {

        TipoArmazenamentoEnum tipo = tipoArmazenamento != null ? tipoArmazenamento : TipoArmazenamentoEnum.ABERTO;

        log.info("Atualizando produto id={} imagens={} tipoArmazenamento={}",
                id, imagens != null ? imagens.size() : 0, tipo);

        ProdutoResponse produto = updateProdutoUseCase.execute(id, request);

        if (imagens != null && !imagens.isEmpty()) {
            List<ImagemUploadRequest> uploadRequests = buildUploadRequests(
                    produto.getId(), produto.getIdEmpresa(), tipo, imagens.size());
            List<ImagemResponse> novasImagens = new ArrayList<>();
            for (int i = 0; i < imagens.size(); i++) {
                try {
                    novasImagens.add(createImagemUseCase.execute(imagens.get(i), uploadRequests.get(i)));
                } catch (Exception e) {
                    log.warn("Falha ao fazer upload da imagem {} no update do produto {}: {}", i, id, e.getMessage());
                }
            }
            produto.setImagens(novasImagens);
        } else {
            // Retornar imagens existentes mesmo sem novo upload
            try {
                List<ImagemResponse> existentes = getImagensByProdutoUseCase.executeAll(id);
                if (!existentes.isEmpty()) {
                    produto.setImagens(existentes);
                }
            } catch (Exception e) {
                log.warn("[ProdutoController] Erro ao buscar imagens existentes do produto {}: {}", id, e.getMessage());
            }
        }

        return ResponseEntity.ok(produto);
    }

    /**
     * Cadastro em lote de produtos com busca automática de imagem na API
     * eanpictures.com.br.
     *
     * <p>
     * Para cada produto da lista:
     * </p>
     * <ol>
     * <li>Busca imagem em
     * {@code http://www.eanpictures.com.br:9000/api/gtin/{codigoEan}}</li>
     * <li>Cria o produto normalmente</li>
     * <li>Se imagem encontrada, faz upload para Cloudflare e vincula ao
     * produto</li>
     * </ol>
     * <p>
     * EANs duplicados são retornados com {@code status=IGNORADO} sem interromper o
     * lote.
     * </p>
     */
    @PostMapping(value = "/lote", consumes = MediaType.APPLICATION_JSON_VALUE)
    @IsUsuario
    @Operation(summary = "Cadastro em lote com busca automática de imagem", description = "Recebe lista de produtos, busca imagem na API eanpictures.com.br pelo EAN, "
            + "cria cada produto e faz upload da imagem para a Cloudflare quando disponível. "
            + "EANs duplicados são ignorados sem erro. Retorna resumo e status de cada item.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lote processado (ver status por item)"),
            @ApiResponse(responseCode = "400", description = "Corpo da requisição inválido"),
            @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    public ResponseEntity<BatchProdutoResponse> criarLote(
            @RequestBody @Valid List<ProdutoRequest> produtos) {
        log.info("Cadastro em lote: {} produto(s)", produtos.size());
        BatchProdutoResponse resposta = batchCreateProdutoUseCase.execute(produtos);
        return ResponseEntity.ok(resposta);
    }

    @DeleteMapping("/{id}")
    @IsUsuario
    @Operation(summary = "Remover produto")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Produto removido"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado")
    })
    public ResponseEntity<Void> remover(@PathVariable String id) {
        log.info("Removendo produto id={}", id);
        deleteProdutoUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }

    // -------------------------------------------------------------------------
    // helpers
    // -------------------------------------------------------------------------

    private List<ImagemUploadRequest> buildUploadRequests(String idProduto, Long idEmpresa,
            TipoArmazenamentoEnum tipoArmazenamento,
            int count) {
        List<ImagemUploadRequest> requests = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            ImagemUploadRequest req = new ImagemUploadRequest();
            req.setIdProduto(idProduto);
            req.setTipoArmazenamento(tipoArmazenamento);
            req.setIdEmpresa(idEmpresa);
            requests.add(req);
        }
        return requests;
    }
}

