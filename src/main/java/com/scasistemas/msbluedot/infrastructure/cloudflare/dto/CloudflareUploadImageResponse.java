package com.scasistemas.msbluedot.infrastructure.cloudflare.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Resposta da API Cloudflare para upload de imagem.
 *
 * <p>
 * Endpoint: POST /accounts/{accountId}/images/v1
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CloudflareUploadImageResponse {

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
        private String filename;
        private String uploaded;

        @JsonProperty("requireSignedURLs")
        private boolean requireSignedURLs;

        private List<String> variants;
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

