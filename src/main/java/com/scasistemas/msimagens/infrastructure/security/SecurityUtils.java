package com.scasistemas.msimagens.infrastructure.security;

import com.scasistemas.msimagens.domain.entities.Usuario;
import com.scasistemas.msimagens.domain.enums.RoleEnum;
import lombok.experimental.UtilityClass;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

/**
 * Utilitários para acesso ao contexto de segurança da requisição atual.
 *
 * <p>
 * Todos os métodos assumem que o {@link JwtAuthenticationFilter} já populou
 * o {@link SecurityContextHolder}. Devem ser chamados apenas dentro do escopo
 * de uma requisição HTTP autenticada.
 * </p>
 */
@UtilityClass
public class SecurityUtils {

    /**
     * Retorna o {@link UserPrincipal} do usuário autenticado.
     *
     * @throws IllegalStateException se não houver usuário autenticado
     */
    public UserPrincipal getCurrentPrincipal() {
        Authentication auth = getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal principal)) {
            throw new IllegalStateException("Nenhum usuário autenticado no contexto");
        }
        return principal;
    }

    /**
     * Retorna a entidade de domínio {@link Usuario} do usuário autenticado.
     */
    public Usuario getCurrentUser() {
        return getCurrentPrincipal().getUsuario();
    }

    /**
     * Retorna o ID da empresa do usuário autenticado.
     */
    public Long getCurrentIdEmpresa() {
        return getCurrentPrincipal().getIdEmpresa();
    }

    /**
     * Retorna o nome (username) do usuário autenticado.
     */
    public String getCurrentUsername() {
        return getCurrentPrincipal().getUsername();
    }

    /**
     * Verifica se o usuário atual possui a role informada.
     */
    public boolean hasRole(RoleEnum role) {
        return role != null && role.equals(getCurrentPrincipal().getRole());
    }

    /**
     * Verifica se o usuário atual é ADMINISTRADOR.
     */
    public boolean isAdministrador() {
        return hasRole(RoleEnum.ADMINISTRADOR);
    }

    /**
     * Retorna o Bearer token da requisição atual (sem o prefixo "Bearer ").
     *
     * <p>
     * O token é extraído dos {@link WebAuthenticationDetails} associados à
     * autenticação. Retorna {@code null} se não disponível.
     * </p>
     */
    public String getCurrentToken() {
        Authentication auth = getAuthentication();
        if (auth != null && auth.getCredentials() instanceof String token) {
            return token;
        }
        return null;
    }

    // ─────────────────────────────────────────────────────────────────

    private Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
}
