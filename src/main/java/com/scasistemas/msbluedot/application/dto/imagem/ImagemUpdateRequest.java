package com.scasistemas.msbluedot.application.dto.imagem;

import com.scasistemas.msbluedot.domain.enums.TipoArmazenamentoEnum;
import jakarta.validation.constraints.NotNull;

public record ImagemUpdateRequest(
        @NotNull(message = "Tipo de armazenamento é obrigatório") TipoArmazenamentoEnum tipoArmazenamento) {
}

