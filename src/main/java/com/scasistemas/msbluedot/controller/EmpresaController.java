package com.scasistemas.msbluedot.controller;

import com.scasistemas.msbluedot.dto.EmpresaRequest;
import com.scasistemas.msbluedot.dto.EmpresaResponse;
import com.scasistemas.msbluedot.security.annotation.IsAdministrador;
import com.scasistemas.msbluedot.security.annotation.IsUsuario;
import com.scasistemas.msbluedot.service.EmpresaService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v2/empresas")
@RequiredArgsConstructor
@Tag(name = "Empresas v2", description = "CRUD de empresas")
public class EmpresaController {

    private final EmpresaService empresaService;

    @PostMapping
    @IsAdministrador
    @Operation(summary = "Criar empresa")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Empresa criada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "403", description = "Acesso negado - requer ADMINISTRADOR"),
            @ApiResponse(responseCode = "409", description = "Código ERP já cadastrado")
    })
    public ResponseEntity<EmpresaResponse> criar(@Valid @RequestBody EmpresaRequest request) {
        log.info("Criando empresa: codigoErp={}", request.getCodigoErp());
        return ResponseEntity.status(HttpStatus.CREATED).body(empresaService.criar(request));
    }

    @GetMapping("/{id}")
    @IsUsuario
    @Operation(summary = "Buscar empresa por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Empresa encontrada"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "404", description = "Empresa não encontrada")
    })
    public ResponseEntity<EmpresaResponse> buscarPorId(@PathVariable Long id) {
        log.info("Buscando empresa id={}", id);
        return ResponseEntity.ok(empresaService.buscarPorId(id));
    }

    @GetMapping
    @IsAdministrador
    @Operation(summary = "Listar todas as empresas")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista paginada de empresas"),
            @ApiResponse(responseCode = "403", description = "Acesso negado - requer ADMINISTRADOR")
    })
    public ResponseEntity<Page<EmpresaResponse>> listar(
            @PageableDefault(size = 20, sort = "nome") Pageable pageable) {
        log.info("Listando empresas page={} size={}", pageable.getPageNumber(), pageable.getPageSize());
        return ResponseEntity.ok(empresaService.listar(pageable));
    }

    @PutMapping("/{id}")
    @IsAdministrador
    @Operation(summary = "Atualizar empresa")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Empresa atualizada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "403", description = "Acesso negado - requer ADMINISTRADOR"),
            @ApiResponse(responseCode = "404", description = "Empresa não encontrada"),
            @ApiResponse(responseCode = "409", description = "Código ERP já em uso")
    })
    public ResponseEntity<EmpresaResponse> atualizar(@PathVariable Long id,
            @Valid @RequestBody EmpresaRequest request) {
        log.info("Atualizando empresa id={}", id);
        return ResponseEntity.ok(empresaService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @IsAdministrador
    @Operation(summary = "Remover empresa")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Empresa removida (soft delete)"),
            @ApiResponse(responseCode = "403", description = "Acesso negado - requer ADMINISTRADOR"),
            @ApiResponse(responseCode = "404", description = "Empresa não encontrada")
    })
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        log.info("Removendo empresa id={}", id);
        empresaService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
