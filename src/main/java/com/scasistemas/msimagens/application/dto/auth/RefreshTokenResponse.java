package com.scasistemas.msimagens.application.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Resposta à renovação de token, com novo access token e refresh token
 * rotacionado.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenResponse {

    /** Novo JWT de curta duração (30 minutos). */
    private String accessToken;

    /** Novo refresh token rotacionado (invalida o anterior). */
    private String refreshToken;

    /** Tipo do token — sempre "Bearer". */
    @Builder.Default
    private String tipo = "Bearer";

    /** Expiração do novo access token em segundos. */
    private Long expiresIn;
}
