package com.scasistemas.msbluedot.infrastructure.web.controllers;

import com.scasistemas.msbluedot.application.dto.auth.LoginRequest;
import com.scasistemas.msbluedot.application.dto.auth.LoginResponse;
import com.scasistemas.msbluedot.application.dto.auth.RefreshTokenRequest;
import com.scasistemas.msbluedot.application.dto.auth.RefreshTokenResponse;
import com.scasistemas.msbluedot.application.usecases.AuthenticateUserUseCase;
import com.scasistemas.msbluedot.application.usecases.LogoutUseCase;
import com.scasistemas.msbluedot.application.usecases.RefreshTokenUseCase;
import com.scasistemas.msbluedot.infrastructure.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller de autenticação JWT.
 *
 * <p>
 * Endpoints públicos (não exigem autenticação):
 * <ul>
 * <li>POST /api/v1/auth/login</li>
 * <li>POST /api/v1/auth/refresh</li>
 * </ul>
 * </p>
 *
 * <p>
 * Endpoints protegidos:
 * <ul>
 * <li>POST /api/v1/auth/logout</li>
 * </ul>
 * </p>
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Login, refresh token e logout")
public class AuthController {

    private final AuthenticateUserUseCase authenticateUserUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final LogoutUseCase logoutUseCase;

    /**
     * Autentica o usuário e retorna access + refresh tokens.
     *
     * @param request credenciais (nome + senha)
     * @param httpReq requisição HTTP (para extrair IP)
     * @return par de tokens JWT
     */
    @PostMapping("/login")
    @Operation(summary = "Login", description = "Autentica com nome e senha, retorna access + refresh tokens", security = {})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Autenticado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas"),
            @ApiResponse(responseCode = "423", description = "Conta bloqueada por excesso de tentativas")
    })
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpReq) {

        String ip = getClientIp(httpReq);
        LoginResponse response = authenticateUserUseCase.execute(request, ip);
        return ResponseEntity.ok(response);
    }

    /**
     * Renova o access token com um refresh token válido (rotação).
     *
     * @param request refresh token
     * @return novo par de tokens
     */
    @PostMapping("/refresh")
    @Operation(summary = "Refresh Token", description = "Renova o access token via refresh token (rotação automática)", security = {})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Novo par de tokens gerado"),
            @ApiResponse(responseCode = "401", description = "Refresh token inválido, expirado ou revogado")
    })
    public ResponseEntity<RefreshTokenResponse> refresh(
            @Valid @RequestBody RefreshTokenRequest request) {

        RefreshTokenResponse response = refreshTokenUseCase.execute(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Invalida os tokens do usuário autenticado (logout).
     *
     * @param refreshTokenRequest body opcional com o refresh token a revogar
     * @param httpReq             requisição HTTP
     * @return 204 No Content
     */
    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Invalida o access token (blacklist) e revoga o refresh token")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Logout realizado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    public ResponseEntity<Void> logout(
            @RequestBody(required = false) RefreshTokenRequest refreshTokenRequest,
            HttpServletRequest httpReq) {

        String username = SecurityUtils.getCurrentUsername();
        Long idEmpresa = SecurityUtils.getCurrentIdEmpresa();
        String ip = getClientIp(httpReq);
        String refreshToken = refreshTokenRequest != null ? refreshTokenRequest.getRefreshToken() : null;

        logoutUseCase.execute(httpReq, refreshToken, username, idEmpresa, ip);
        return ResponseEntity.noContent().build();
    }

    // ─────────────────────────────────────────────────────────────────

    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}

