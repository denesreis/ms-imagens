package com.scasistemas.msbluedot.infrastructure.security;

import com.scasistemas.msbluedot.domain.entities.Usuario;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

/**
 * Factory que usa {@link WithMockUserPrincipal} para popular o SecurityContext
 * com um {@link UserPrincipal} compatível com {@link SecurityUtils}.
 */
public class WithMockUserPrincipalSecurityContextFactory
        implements WithSecurityContextFactory<WithMockUserPrincipal> {

    @Override
    public SecurityContext createSecurityContext(WithMockUserPrincipal annotation) {
        Usuario usuario = Usuario.builder()
                .id(annotation.id())
                .nome(annotation.nome())
                .role(annotation.role())
                .idEmpresa(annotation.idEmpresa())
                .ativo(true)
                .senha("N/A")
                .build();

        UserPrincipal principal = new UserPrincipal(usuario);

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities());

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        return context;
    }
}

