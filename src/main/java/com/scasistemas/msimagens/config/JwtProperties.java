package com.scasistemas.msimagens.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Propriedades de configuração do JWT.
 *
 * <p>
 * Mapeadas do prefixo <code>app.jwt</code> no application.yml.
 * </p>
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    /**
     * Secret para assinar os tokens JWT.
     * Mínimo 256 bits para HS256.
     * OBRIGATÓRIO via env: JWT_SECRET (nunca usar valor padrão em produção!).
     */
    private String secret;

    /** Expiração do access token em ms (padrão: 30 minutos = 1.800.000 ms) */
    private Long expiration = 1_800_000L;

    /** Expiração do refresh token em ms (padrão: 7 dias = 604.800.000 ms) */
    private Long refreshExpiration = 604_800_000L;
}
