package com.scasistemas.msbluedot.dto;

import com.scasistemas.msbluedot.enums.TipoArmazenamentoEnum;
import jakarta.validation.constraints.NotNull;

public record ImagemUpdateRequest(
        @NotNull(message = "Tipo de armazenamento é obrigatório") TipoArmazenamentoEnum tipoArmazenamento) {
}
