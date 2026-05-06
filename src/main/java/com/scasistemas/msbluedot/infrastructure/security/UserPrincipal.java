package com.scasistemas.msbluedot.infrastructure.security;

import com.scasistemas.msbluedot.domain.entities.Usuario;
import com.scasistemas.msbluedot.domain.enums.RoleEnum;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Wrapper da entidade de domínio {@link Usuario} para integração com o
 * Spring Security {@link UserDetails}.
 *
 * <p>
 * Fornece authorities no formato {@code ROLE_ADMINISTRADOR} ou
 * {@code ROLE_USUARIO} exigido pelo Spring Security.
 * </p>
 *
 * <p>
 * O bloqueio de conta é derivado diretamente do campo {@code bloqueadoAte}
 * da entidade de domínio, sem necessidade de persistência adicional.
 * </p>
 */
public class UserPrincipal implements UserDetails {

    /** Entidade de domínio subjacente. */
    @Getter
    private final Usuario usuario;

    public UserPrincipal(Usuario usuario) {
        this.usuario = usuario;
    }

    // ─────────────────────────────────────────────────────────────────
    // UserDetails
    // ─────────────────────────────────────────────────────────────────

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        RoleEnum role = usuario.getRole();
        String authority = "ROLE_" + (role != null ? role.name() : RoleEnum.USUARIO.name());
        return List.of(new SimpleGrantedAuthority(authority));
    }

    @Override
    public String getPassword() {
        return usuario.getSenha();
    }

    @Override
    public String getUsername() {
        return usuario.getNome();
    }

    @Override
    public boolean isAccountNonExpired() {
        return Boolean.TRUE.equals(usuario.getAtivo());
    }

    @Override
    public boolean isAccountNonLocked() {
        return !usuario.estaBloqueado();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return Boolean.TRUE.equals(usuario.getAtivo());
    }

    // ─────────────────────────────────────────────────────────────────
    // Utilitários
    // ─────────────────────────────────────────────────────────────────

    public Long getId() {
        return usuario.getId();
    }

    public Long getIdEmpresa() {
        return usuario.getIdEmpresa();
    }

    public RoleEnum getRole() {
        return usuario.getRole();
    }

    public boolean isAdministrador() {
        return RoleEnum.ADMINISTRADOR.equals(usuario.getRole());
    }
}

