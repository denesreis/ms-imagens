package com.scasistemas.msbluedot.controller;

import com.scasistemas.msbluedot.dto.UsuarioRequest;
import com.scasistemas.msbluedot.dto.UsuarioResponse;
import com.scasistemas.msbluedot.security.annotation.IsAdministrador;
import com.scasistemas.msbluedot.security.annotation.IsUsuario;
import com.scasistemas.msbluedot.service.UsuarioService;
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
@RequestMapping("/api/v2/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuários v2", description = "CRUD de usuários")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping
    @IsAdministrador
    @Operation(summary = "Criar usuário")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuário criado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "403", description = "Acesso negado - requer ADMINISTRADOR"),
            @ApiResponse(responseCode = "409", description = "Nome de usuário já cadastrado")
    })
    public ResponseEntity<UsuarioResponse> criar(@Valid @RequestBody UsuarioRequest request) {
        log.info("Criando usuário: nome={}", request.getNome());
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.criar(request));
    }

    @GetMapping("/{id}")
    @IsUsuario
    @Operation(summary = "Buscar usuário por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário encontrado"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    public ResponseEntity<UsuarioResponse> buscarPorId(@PathVariable Long id) {
        log.info("Buscando usuário id={}", id);
        return ResponseEntity.ok(usuarioService.buscarPorId(id));
    }

    @GetMapping
    @IsUsuario
    @Operation(summary = "Listar usuários")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista paginada de usuários"),
            @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    public ResponseEntity<Page<UsuarioResponse>> listar(
            @PageableDefault(size = 20, sort = "nome") Pageable pageable) {
        log.info("Listando usuários page={} size={}", pageable.getPageNumber(), pageable.getPageSize());
        return ResponseEntity.ok(usuarioService.listar(pageable));
    }

    @PutMapping("/{id}")
    @IsUsuario
    @Operation(summary = "Atualizar usuário")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário atualizado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    public ResponseEntity<UsuarioResponse> atualizar(@PathVariable Long id,
            @Valid @RequestBody UsuarioRequest request) {
        log.info("Atualizando usuário id={}", id);
        return ResponseEntity.ok(usuarioService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @IsAdministrador
    @Operation(summary = "Remover usuário")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Usuário removido (soft delete)"),
            @ApiResponse(responseCode = "403", description = "Acesso negado - requer ADMINISTRADOR"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        log.info("Removendo usuário id={}", id);
        usuarioService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
