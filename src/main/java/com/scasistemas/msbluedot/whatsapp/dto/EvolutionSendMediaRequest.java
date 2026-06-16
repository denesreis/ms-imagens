package com.scasistemas.msbluedot.whatsapp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EvolutionSendMediaRequest {

    @JsonProperty("number")
    private String number;

    @JsonProperty("mediatype")
    private String mediatype;

    @JsonProperty("mimetype")
    private String mimetype;

    @JsonProperty("caption")
    private String caption;

    @JsonProperty("media")
    private String media;

    @JsonProperty("fileName")
    private String fileName;
}
