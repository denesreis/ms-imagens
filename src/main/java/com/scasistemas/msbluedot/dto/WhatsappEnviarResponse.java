package com.scasistemas.msbluedot.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class WhatsappEnviarResponse {

    private String status;
    private String telefone;
    private String mensagemId;
    private Instant timestamp;
}
