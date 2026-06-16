package com.scasistemas.msbluedot.controller;

import com.scasistemas.msbluedot.dto.LoginRequest;
import com.scasistemas.msbluedot.dto.LoginResponse;
import com.scasistemas.msbluedot.dto.RefreshTokenRequest;
import com.scasistemas.msbluedot.dto.RefreshTokenResponse;
import com.scasistemas.msbluedot.security.SecurityUtils;
import com.scasistemas.msbluedot.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

@Slf4j
@RestController
@RequestMapping("/api/v2/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação v2", description = "Login, refresh token e logout")
public class AuthController {

    private final AuthService authService;

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
        return ResponseEntity.ok(authService.login(request, ip));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh Token", description = "Renova o access token via refresh token (rotação automática)", security = {})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Novo par de tokens gerado"),
            @ApiResponse(responseCode = "401", description = "Refresh token inválido, expirado ou revogado")
    })
    public ResponseEntity<RefreshTokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refresh(request));
    }

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
        authService.logout(httpReq, refreshToken, username, idEmpresa, ip);
        return ResponseEntity.noContent().build();
    }

    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
