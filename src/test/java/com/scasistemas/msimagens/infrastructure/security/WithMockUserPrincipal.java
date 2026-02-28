package com.scasistemas.msimagens.infrastructure.security;

import com.scasistemas.msimagens.domain.enums.RoleEnum;
import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotação para testes que popula o SecurityContext com um
 * {@link UserPrincipal}
 * real, compatível com {@link SecurityUtils}.
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@WithSecurityContext(factory = WithMockUserPrincipalSecurityContextFactory.class)
public @interface WithMockUserPrincipal {

    long id() default 1L;

    String nome() default "test.user";

    RoleEnum role() default RoleEnum.USUARIO;

    long idEmpresa() default 1L;
}
