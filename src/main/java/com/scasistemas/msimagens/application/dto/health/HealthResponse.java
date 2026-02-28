package com.scasistemas.msimagens.application.dto.health;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record HealthResponse(
        String status,
        String banco,
        String cloudflare,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime timestamp) {
    public static HealthResponse up(String banco, String cloudflare) {
        return new HealthResponse("UP", banco, cloudflare, LocalDateTime.now());
    }

    public static HealthResponse down(String banco, String cloudflare) {
        return new HealthResponse("DOWN", banco, cloudflare, LocalDateTime.now());
    }
}
