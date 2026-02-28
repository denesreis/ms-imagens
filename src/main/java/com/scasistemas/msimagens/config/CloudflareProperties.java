package com.scasistemas.msimagens.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Propriedades de configuração da integração com a Cloudflare Images API.
 *
 * <p>
 * Mapeadas do prefixo <code>app.cloudflare</code> no application.yml.
 * </p>
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app.cloudflare")
public class CloudflareProperties {

    /** ID da conta Cloudflare (obrigatório via env: CLOUDFLARE_ACCOUNT_ID) */
    private String accountId;

    /** URL base da API Cloudflare */
    private String baseUrl = "https://api.cloudflare.com/client/v4";

    /** Token para operações de leitura (env: CLOUDFLARE_GET_TOKEN) */
    private String getToken;

    /** Token para operações de escrita/upload (env: CLOUDFLARE_POST_TOKEN) */
    private String postToken;

    /** Tamanho máximo de arquivo em bytes (padrão: 10MB) */
    private Long maxFileSize = 10485760L;

    /** Extensões aceitas separadas por vírgula */
    private String allowedExtensions = "jpg,jpeg,png,gif,webp";

    /** Configurações de timeout */
    private Timeout timeout = new Timeout();

    @Data
    public static class Timeout {
        /** Timeout de conexão em ms (padrão: 10 segundos) */
        private Integer connect = 10000;

        /** Timeout de leitura em ms (padrão: 30 segundos) */
        private Integer read = 30000;
    }
}
