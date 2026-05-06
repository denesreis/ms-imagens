package com.scasistemas.msbluedot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Propriedades de configuração de segurança.
 *
 * <p>
 * Mapeadas do prefixo <code>app.security</code> no application.yml.
 * </p>
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app.security")
public class SecurityProperties {

    /** Configurações de proteção contra brute force no login */
    private Login login = new Login();

    @Data
    public static class Login {
        /**
         * Número máximo de tentativas de login antes de bloquear a conta.
         * Padrão: 5 tentativas.
         */
        private int maxAttempts = 5;

        /**
         * Duração do bloqueio em milissegundos após atingir maxAttempts.
         * Padrão: 900.000 ms = 15 minutos.
         */
        private long blockDuration = 900_000L;
    }
}

