package com.scasistemas.msbluedot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class WhatsappEnviarRequest {

    @NotBlank(message = "Telefone é obrigatório")
    @Pattern(regexp = "\\d{10,13}", message = "Telefone deve conter apenas dígitos (10–13 caracteres, ex: 5511999999999)")
    private String telefone;

    @NotBlank(message = "Mensagem é obrigatória")
    @Size(max = 4096, message = "Mensagem deve ter no máximo 4096 caracteres")
    private String mensagem;
}
