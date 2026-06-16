package com.scasistemas.msbluedot.security;

import com.scasistemas.msbluedot.entity.Usuario;
import com.scasistemas.msbluedot.enums.RoleEnum;
import lombok.experimental.UtilityClass;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@UtilityClass
public class SecurityUtils {

    public UserPrincipal getCurrentPrincipal() {
        Authentication auth = getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal principal)) {
            throw new IllegalStateException("Nenhum usuário autenticado no contexto");
        }
        return principal;
    }

    public Usuario getCurrentUser() {
        return getCurrentPrincipal().getUsuario();
    }

    public Long getCurrentIdEmpresa() {
        return getCurrentPrincipal().getIdEmpresa();
    }

    public String getCurrentUsername() {
        return getCurrentPrincipal().getUsername();
    }

    public boolean hasRole(RoleEnum role) {
        return role != null && role.equals(getCurrentPrincipal().getRole());
    }

    public boolean isAdministrador() {
        return hasRole(RoleEnum.ADMINISTRADOR);
    }

    public String getCurrentToken() {
        Authentication auth = getAuthentication();
        if (auth != null && auth.getCredentials() instanceof String token) {
            return token;
        }
        return null;
    }

    private Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
}
