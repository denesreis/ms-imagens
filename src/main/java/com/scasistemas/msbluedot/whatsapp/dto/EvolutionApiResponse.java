package com.scasistemas.msbluedot.whatsapp.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EvolutionApiResponse {

    @JsonProperty("key")
    private Key key;

    @JsonProperty("status")
    private String status;

    @JsonProperty("messageTimestamp")
    private String messageTimestamp;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Key {

        @JsonProperty("id")
        private String id;

        @JsonProperty("remoteJid")
        private String remoteJid;

        @JsonProperty("fromMe")
        private Boolean fromMe;
    }

    public String getMensagemId() {
        return key != null ? key.getId() : null;
    }
}
