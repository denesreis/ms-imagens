package com.scasistemas.msbluedot.infrastructure.security.annotation;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Permite acesso aos perfis {@code USUARIO} e {@code ADMINISTRADOR}.
 *
 * <p>
 * Equivalente a {@code @PreAuthorize("hasAnyRole('USUARIO','ADMINISTRADOR')")}.
 * </p>
 *
 * <p>
 * Exemplo de uso:
 * 
 * <pre>
 * {@code @IsUsuario}
 * {@code @GetMapping("/produtos/{id}")}
 * public ResponseEntity<ProdutoResponse> buscarProduto(...) { ... }
 * </pre>
 * </p>
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@PreAuthorize("hasAnyRole('USUARIO','ADMINISTRADOR')")
public @interface IsUsuario {
}

