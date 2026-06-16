package com.scasistemas.msbluedot.controller;

import com.scasistemas.msbluedot.dto.ImagemProdutoResponse;
import com.scasistemas.msbluedot.dto.ImagemResponse;
import com.scasistemas.msbluedot.dto.ImagemUpdateRequest;
import com.scasistemas.msbluedot.dto.ImagemUploadRequest;
import com.scasistemas.msbluedot.security.annotation.IsUsuario;
import com.scasistemas.msbluedot.service.ImagemService;
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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v2/imagens")
@RequiredArgsConstructor
@Tag(name = "Imagens v2", description = "Upload e gestão de imagens de produtos")
public class ImagemController {

    private final ImagemService imagemService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @IsUsuario
    @Operation(summary = "Fazer upload de imagem para um produto")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Imagem enviada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Arquivo inválido ou dados ausentes"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado")
    })
    public ResponseEntity<ImagemResponse> upload(
            @RequestPart("arquivo") MultipartFile arquivo,
            @RequestPart("dados") @Valid ImagemUploadRequest dados) throws IOException {
        log.info("Upload imagem idProduto={} tipo={}", dados.getIdProduto(), dados.getTipoArmazenamento());
        return ResponseEntity.status(HttpStatus.CREATED).body(imagemService.upload(arquivo, dados));
    }

    @GetMapping("/ean/{codigoEan}")
    @Operation(summary = "Listar imagens por código EAN (endpoint público)",
            description = "Retorna apenas imagens com tipo ABERTO e status ATIVO. Não requer autenticação.", security = {})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de imagens (pode ser vazia)")
    })
    public ResponseEntity<List<ImagemProdutoResponse>> buscarPorEan(@PathVariable String codigoEan) {
        log.info("Buscando imagens codigoEan={}", codigoEan);
        return ResponseEntity.ok(imagemService.buscarPorEan(codigoEan));
    }

    @GetMapping("/produto/{idProduto}")
    @IsUsuario
    @Operation(summary = "Listar imagens de um produto (paginado)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista paginada de imagens"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado")
    })
    public ResponseEntity<Page<ImagemResponse>> buscarPorProduto(
            @PathVariable String idProduto,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("Buscando imagens idProduto={}", idProduto);
        return ResponseEntity.ok(imagemService.buscarPorProduto(idProduto, pageable));
    }

    @GetMapping("/{id}")
    @IsUsuario
    @Operation(summary = "Buscar imagem por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Imagem encontrada"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "404", description = "Imagem não encontrada")
    })
    public ResponseEntity<ImagemResponse> buscarPorId(@PathVariable String id) {
        log.info("Buscando imagem id={}", id);
        return ResponseEntity.ok(imagemService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    @IsUsuario
    @Operation(summary = "Atualizar tipo de armazenamento da imagem")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Imagem atualizada"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Imagem não encontrada")
    })
    public ResponseEntity<ImagemResponse> atualizar(@PathVariable String id,
            @Valid @RequestBody ImagemUpdateRequest request) {
        log.info("Atualizando imagem id={} tipoArmazenamento={}", id, request.tipoArmazenamento());
        return ResponseEntity.ok(imagemService.atualizar(id, request.tipoArmazenamento()));
    }

    @DeleteMapping("/{id}")
    @IsUsuario
    @Operation(summary = "Remover imagem")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Imagem removida"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Imagem não encontrada")
    })
    public ResponseEntity<Void> remover(@PathVariable String id) {
        log.info("Removendo imagem id={}", id);
        imagemService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
