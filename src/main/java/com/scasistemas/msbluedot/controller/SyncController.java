package com.scasistemas.msbluedot.controller;

import com.scasistemas.msbluedot.dto.EmpresaResponse;
import com.scasistemas.msbluedot.dto.RefreshTokenResponse;
import com.scasistemas.msbluedot.dto.SincronizarEmpresaRequest;
import com.scasistemas.msbluedot.dto.SincronizarUsuarioRequest;
import com.scasistemas.msbluedot.dto.SyncTokenRequest;
import com.scasistemas.msbluedot.dto.UsuarioResponse;
import com.scasistemas.msbluedot.service.SyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v2/sync")
@RequiredArgsConstructor
@Tag(name = "Sync v2", description = "Endpoints internos de sincronização entre serviços")
public class SyncController {

    private final SyncService syncService;

    @PostMapping("/usuario")
    @Operation(summary = "Upsert de usuário (sincronização interna)",
            description = "Cria ou atualiza um usuário com base no nome informado. Acesso restrito por X-Sync-Key.")
    public ResponseEntity<UsuarioResponse> sincronizarUsuario(
            @Parameter(description = "Dados do usuário a sincronizar")
            @Valid @RequestBody SincronizarUsuarioRequest request) {
        return ResponseEntity.ok(syncService.sincronizarUsuario(request));
    }

    @PostMapping("/empresa")
    @Operation(summary = "Upsert de empresa (sincronização interna)",
            description = "Cria ou atualiza uma empresa com base no codigoErp informado. Acesso restrito por X-Sync-Key.")
    public ResponseEntity<EmpresaResponse> sincronizarEmpresa(
            @Parameter(description = "Dados da empresa a sincronizar")
            @Valid @RequestBody SincronizarEmpresaRequest request) {
        return ResponseEntity.ok(syncService.sincronizarEmpresa(request));
    }

    @PostMapping("/tokens")
    @Operation(summary = "Emitir tokens para usuário (reconexão automática)",
            description = "Gera um novo par de tokens (access + refresh) para o usuário. Acesso restrito por X-Sync-Key.")
    public ResponseEntity<RefreshTokenResponse> emitirTokens(
            @Parameter(description = "Dados do usuário para emissão de tokens")
            @Valid @RequestBody SyncTokenRequest request) {
        return ResponseEntity.ok(syncService.emitirTokens(request));
    }
}
