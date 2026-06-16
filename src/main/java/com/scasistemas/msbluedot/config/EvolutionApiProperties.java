package com.scasistemas.msbluedot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.whatsapp")
public class EvolutionApiProperties {

    private String apiUrl;
    private String apiKey;
    private String instance;
    private int maxFileSizeMb = 10;
    private Timeout timeout = new Timeout();

    @Data
    public static class Timeout {
        private int connect = 10000;
        private int read = 30000;
    }
}
