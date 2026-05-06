package com.scasistemas.msbluedot.infrastructure.cloudflare.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Resposta da API Cloudflare para verificação de token.
 *
 * <p>
 * Endpoint: GET /user/tokens/verify
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CloudflareTokenVerifyResponse {

    private Result result;
    private boolean success;
    private List<CloudflareApiError> errors;
    private List<CloudflareApiMessage> messages;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Result {
        private String id;
        private String status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CloudflareApiError {
        private Integer code;
        private String message;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CloudflareApiMessage {
        private Integer code;
        private String message;
    }
}

