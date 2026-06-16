package com.scasistemas.msbluedot.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados para emissão de tokens via sync interno")
public class SyncTokenRequest {

    @NotBlank(message = "Nome é obrigatório")
    @Schema(description = "Username do usuário (mesmo do novo-mercador)", example = "joao.silva")
    private String nome;
}
