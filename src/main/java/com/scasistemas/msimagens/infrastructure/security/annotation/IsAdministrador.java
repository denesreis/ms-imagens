package com.scasistemas.msimagens.infrastructure.security.annotation;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Restringe o acesso ao perfil {@code ADMINISTRADOR}.
 *
 * <p>
 * Equivalente a {@code @PreAuthorize("hasRole('ADMINISTRADOR')")}.
 * </p>
 *
 * <p>
 * Exemplo de uso:
 * 
 * <pre>
 * {@code @IsAdministrador}
 * {@code @PostMapping("/usuarios")}
 * public ResponseEntity<UsuarioResponse> criarUsuario(...) { ... }
 * </pre>
 * </p>
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@PreAuthorize("hasRole('ADMINISTRADOR')")
public @interface IsAdministrador {
}
