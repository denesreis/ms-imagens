package com.scasistemas.msbluedot;

import com.scasistemas.msbluedot.entity.Usuario;
import com.scasistemas.msbluedot.enums.RoleEnum;
import com.scasistemas.msbluedot.security.UserPrincipal;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;

public class TestSecurityHelper {

    public static void mockAdminContext() {
        mockUserContext(1L, "admin", RoleEnum.ADMINISTRADOR, 1L);
    }

    public static void mockUsuarioContext() {
        mockUserContext(2L, "usuario.teste", RoleEnum.USUARIO, 1L);
    }

    public static void mockUserContext(Long id, String nome, RoleEnum role, Long idEmpresa) {
        Usuario usuario = Usuario.builder()
                .id(id)
                .nome(nome)
                .role(role)
                .idEmpresa(idEmpresa)
                .ativo(true)
                .tentativasLogin(0)
                .build();

        UserPrincipal principal = new UserPrincipal(usuario);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities());

        SecurityContextHolder.setContext(new SecurityContextImpl(authentication));
    }

    public static void clearContext() {
        SecurityContextHolder.clearContext();
    }
}
