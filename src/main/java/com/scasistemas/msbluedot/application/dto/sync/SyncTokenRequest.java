package com.scasistemas.msbluedot.application.dto.sync;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Requisição para emissão de tokens via sincronização interna.
 * Recebida no endpoint POST /api/v1/sync/tokens.
 */
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
