package com.scasistemas.msbluedot.application.dto.auth;

import com.scasistemas.msbluedot.domain.enums.RoleEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Resposta ao login bem-sucedido, contendo access e refresh tokens.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    /** JWT de curta duração (30 minutos). */
    private String accessToken;

    /** Token opaco de renovação (7 dias). */
    private String refreshToken;

    /** Tipo do token — sempre "Bearer". */
    @Builder.Default
    private String tipo = "Bearer";

    /** Expiração do access token em segundos. */
    private Long expiresIn;

    /** Expiração do refresh token em segundos. */
    private Long refreshExpiresIn;

    /** Dados do usuário autenticado. */
    private UsuarioInfo usuario;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UsuarioInfo {
        private Long id;
        private String nome;
        private RoleEnum role;
        private Long idEmpresa;
    }
}

