package com.scasistemas.msbluedot.whatsapp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EvolutionSendTextRequest {

    @JsonProperty("number")
    private String number;

    @JsonProperty("text")
    private String text;
}
