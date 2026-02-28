package com.scasistemas.msimagens.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidade de domínio que representa um refresh token de autenticação.
 *
 * <p>
 * <strong>Segurança:</strong> apenas o hash do token é armazenado,
 * nunca o valor plain. Use SHA-256 antes de persistir.
 * </p>
 *
 * <p>
 * Tokens expirados ou revogados devem ser ignorados na validação.
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {

    private Long id;

    /** ID do usuário ao qual o token pertence. */
    private Long idUsuario;

    /**
     * Hash SHA-256 do refresh token.
     * Máximo 255 caracteres. Nunca armazenar o token em plain text.
     */
    private String tokenHash;

    /** Data/hora de expiração do token. */
    private LocalDateTime expiraEm;

    /**
     * Indica se o token foi explicitamente revogado (logout).
     * Token revogado não pode ser utilizado mesmo que não tenha expirado.
     */
    @Builder.Default
    private Boolean revogado = false;

    private LocalDateTime dataCriacao;

    // =====================================================================
    // Métodos de domínio
    // =====================================================================

    /** Verifica se o token está válido (não expirado e não revogado). */
    public boolean estaValido() {
        return !Boolean.TRUE.equals(revogado)
                && expiraEm != null
                && LocalDateTime.now().isBefore(expiraEm);
    }

    /** Revoga o token explicitamente. */
    public void revogar() {
        this.revogado = true;
    }
}
