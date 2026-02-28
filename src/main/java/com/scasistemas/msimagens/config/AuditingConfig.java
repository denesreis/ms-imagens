package com.scasistemas.msimagens.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * Configuração de auditoria JPA.
 *
 * <p>
 * Habilita o preenchimento automático de campos {@code @CreatedDate},
 * {@code @LastModifiedDate} e {@code @CreatedBy} nas entidades JPA.
 * </p>
 *
 * <p>
 * A anotação {@code @EnableJpaAuditing} está na classe principal
 * {@code MsImagensApplication} para evitar conflitos em testes.
 * </p>
 */
@Configuration
public class AuditingConfig {

    /**
     * Provedor do auditor atual.
     *
     * <p>
     * Retorna o nome do usuário autenticado via SecurityContext,
     * ou "sistema" quando não há usuário autenticado (ex: Flyway migrations).
     * </p>
     */
    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()
                    || "anonymousUser".equals(authentication.getPrincipal())) {
                return Optional.of("sistema");
            }
            return Optional.of(authentication.getName());
        };
    }

}
