package com.scasistemas.msbluedot.infrastructure.web.controllers;

import com.scasistemas.msbluedot.application.dto.auth.RefreshTokenResponse;
import com.scasistemas.msbluedot.application.dto.empresa.EmpresaResponse;
import com.scasistemas.msbluedot.application.dto.sync.SincronizarEmpresaRequest;
import com.scasistemas.msbluedot.application.dto.sync.SincronizarUsuarioRequest;
import com.scasistemas.msbluedot.application.dto.sync.SyncTokenRequest;
import com.scasistemas.msbluedot.application.dto.usuario.UsuarioResponse;
import com.scasistemas.msbluedot.application.usecases.EmitirTokensSyncUseCase;
import com.scasistemas.msbluedot.application.usecases.SincronizarEmpresaUseCase;
import com.scasistemas.msbluedot.application.usecases.SincronizarUsuarioUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoints internos de sincronização — protegidos por {@code X-Sync-Key} via
 * {@code SyncApiKeyFilter}. Não exigem JWT.
 */
@RestController
@RequestMapping("/api/v1/sync")
@RequiredArgsConstructor
@Tag(name = "Sync", description = "Endpoints internos de sincronização entre serviços")
public class SyncController {

    private final SincronizarUsuarioUseCase sincronizarUsuarioUseCase;
    private final SincronizarEmpresaUseCase sincronizarEmpresaUseCase;
    private final EmitirTokensSyncUseCase emitirTokensSyncUseCase;

    /**
     * Cria ou atualiza (upsert) um usuário sincronizado do novo-mercador.
     */
    @PostMapping("/usuario")
    @Operation(summary = "Upsert de usuário (sincronização interna)", description = "Cria ou atualiza um usuário com base no nome informado. "
            + "Acesso restrito por X-Sync-Key (header).")
    public ResponseEntity<UsuarioResponse> sincronizarUsuario(
            @Parameter(description = "Dados do usuário a sincronizar") @Valid @RequestBody SincronizarUsuarioRequest request) {

        UsuarioResponse response = sincronizarUsuarioUseCase.execute(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Cria ou atualiza (upsert) uma empresa sincronizada do novo-mercador.
     */
    @PostMapping("/empresa")
    @Operation(summary = "Upsert de empresa (sincronização interna)", description = "Cria ou atualiza uma empresa com base no codigoErp informado. "
            + "Acesso restrito por X-Sync-Key (header).")
    public ResponseEntity<EmpresaResponse> sincronizarEmpresa(
            @Parameter(description = "Dados da empresa a sincronizar") @Valid @RequestBody SincronizarEmpresaRequest request) {

        EmpresaResponse response = sincronizarEmpresaUseCase.execute(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Emite access token + refresh token para um usuário existente sem validação de
     * senha.
     * Chamado pelo novo-mercador para reconexão automática com o Bluedot.
     */
    @PostMapping("/tokens")
    @Operation(summary = "Emitir tokens para usuário (reconexão automática)", description = "Gera um novo par de tokens (access + refresh) para o usuário informado. "
            + "Acesso restrito por X-Sync-Key (header). Não exige senha.")
    public ResponseEntity<RefreshTokenResponse> emitirTokens(
            @Parameter(description = "Dados do usuário para emissão de tokens") @Valid @RequestBody SyncTokenRequest request) {

        RefreshTokenResponse response = emitirTokensSyncUseCase.execute(request);
        return ResponseEntity.ok(response);
    }
}
