package com.scasistemas.msbluedot.controller;

import com.scasistemas.msbluedot.dto.BatchProdutoResponse;
import com.scasistemas.msbluedot.dto.ImagemResponse;
import com.scasistemas.msbluedot.dto.ImagemUploadRequest;
import com.scasistemas.msbluedot.dto.ProdutoRequest;
import com.scasistemas.msbluedot.dto.ProdutoResponse;
import com.scasistemas.msbluedot.enums.TipoArmazenamentoEnum;
import com.scasistemas.msbluedot.security.annotation.IsUsuario;
import com.scasistemas.msbluedot.service.ImagemService;
import com.scasistemas.msbluedot.service.ProdutoService;
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
@RequestMapping("/api/v2/produtos")
@RequiredArgsConstructor
@Tag(name = "Produtos v2", description = "CRUD de produtos com upload de imagens")
public class ProdutoController {

    private final ProdutoService produtoService;
    private final ImagemService imagemService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @IsUsuario
    @Operation(summary = "Criar produto (JSON, sem imagem)",
            description = "Envio application/json. Para incluir imagens, use multipart/form-data.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Produto criado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "409", description = "Código EAN já cadastrado")
    })
    public ResponseEntity<ProdutoResponse> criarJson(
            @RequestBody @Valid ProdutoRequest produtoRequest) {
        log.info("Criando produto (JSON) codigoEan={}", produtoRequest.getCodigoEan());
        return ResponseEntity.status(HttpStatus.CREATED).body(produtoService.criar(produtoRequest));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @IsUsuario
    @Operation(summary = "Criar produto com upload de imagens (opcional)",
            description = "Envio multipart/form-data. Campo 'produto' = JSON do produto. "
                    + "Campo 'imagens' = arquivos (opcional). "
                    + "Param 'tipoArmazenamento' = ABERTO (padrão) ou PRIVADO.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Produto criado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "409", description = "Código EAN já cadastrado")
    })
    public ResponseEntity<ProdutoResponse> criar(
            @RequestPart("produto") @Valid ProdutoRequest produtoRequest,
            @RequestPart(name = "imagens", required = false) List<MultipartFile> imagens,
            @RequestParam(name = "tipoArmazenamento", required = false) TipoArmazenamentoEnum tipoArmazenamento)
            throws IOException {

        TipoArmazenamentoEnum tipo = tipoArmazenamento != null ? tipoArmazenamento : TipoArmazenamentoEnum.ABERTO;
        log.info("Criando produto codigoEan={} imagens={} tipoArmazenamento={}",
                produtoRequest.getCodigoEan(), imagens != null ? imagens.size() : 0, tipo);

        ProdutoResponse produto = produtoService.criar(produtoRequest);

        if (imagens != null && !imagens.isEmpty()) {
            List<ImagemResponse> imagensResponse = new ArrayList<>();
            for (MultipartFile imagem : imagens) {
                try {
                    ImagemUploadRequest uploadRequest = ImagemUploadRequest.builder()
                            .idProduto(produto.getId())
                            .tipoArmazenamento(tipo)
                            .idEmpresa(produto.getIdEmpresa())
                            .build();
                    imagensResponse.add(imagemService.upload(imagem, uploadRequest));
                } catch (Exception e) {
                    log.warn("Falha ao fazer upload da imagem: {}", e.getMessage());
                }
            }
            produto.setImagens(imagensResponse);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(produto);
    }

    @GetMapping("/{id}")
    @IsUsuario
    @Operation(summary = "Buscar produto por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Produto encontrado"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado")
    })
    public ResponseEntity<ProdutoResponse> buscarPorId(@PathVariable String id) {
        log.info("Buscando produto id={}", id);
        ProdutoResponse produto = produtoService.buscarPorId(id);

        try {
            List<ImagemResponse> imagens = imagemService.buscarTodosPorProduto(id);
            if (!imagens.isEmpty()) {
                produto.setImagens(imagens);
            }
        } catch (Exception e) {
            log.warn("Erro ao buscar imagens do produto {}: {}", id, e.getMessage());
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
        return ResponseEntity.ok(produtoService.listar(pageable));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @IsUsuario
    @Operation(summary = "Atualizar produto com upload de imagens (opcional)",
            description = "Envio multipart/form-data. Campo 'produto' = JSON. Campo 'imagens' = novos arquivos (opcional). "
                    + "As imagens existentes são mantidas; as novas são adicionadas.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Produto atualizado"),
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

        ProdutoResponse produto = produtoService.atualizar(id, request);

        if (imagens != null && !imagens.isEmpty()) {
            List<ImagemResponse> novasImagens = new ArrayList<>();
            for (MultipartFile imagem : imagens) {
                try {
                    ImagemUploadRequest uploadRequest = ImagemUploadRequest.builder()
                            .idProduto(produto.getId())
                            .tipoArmazenamento(tipo)
                            .idEmpresa(produto.getIdEmpresa())
                            .build();
                    novasImagens.add(imagemService.upload(imagem, uploadRequest));
                } catch (Exception e) {
                    log.warn("Falha ao fazer upload da imagem no update do produto {}: {}", id, e.getMessage());
                }
            }
            produto.setImagens(novasImagens);
        } else {
            try {
                List<ImagemResponse> existentes = imagemService.buscarTodosPorProduto(id);
                if (!existentes.isEmpty()) {
                    produto.setImagens(existentes);
                }
            } catch (Exception e) {
                log.warn("Erro ao buscar imagens existentes do produto {}: {}", id, e.getMessage());
            }
        }

        return ResponseEntity.ok(produto);
    }

    @PostMapping(value = "/lote", consumes = MediaType.APPLICATION_JSON_VALUE)
    @IsUsuario
    @Operation(summary = "Cadastro em lote com busca automática de imagem",
            description = "Recebe lista de produtos, busca imagem na API eanpictures.com.br pelo EAN. "
                    + "EANs duplicados são ignorados sem erro. Retorna resumo e status de cada item.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lote processado (ver status por item)"),
            @ApiResponse(responseCode = "400", description = "Corpo da requisição inválido"),
            @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    public ResponseEntity<BatchProdutoResponse> criarLote(
            @RequestBody @Valid List<ProdutoRequest> produtos) {
        log.info("Cadastro em lote: {} produto(s)", produtos.size());
        return ResponseEntity.ok(produtoService.criarLote(produtos));
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
        produtoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
