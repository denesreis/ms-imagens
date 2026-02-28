package com.scasistemas.msimagens.application.dto.imagem;

import com.scasistemas.msimagens.domain.enums.TipoArmazenamentoEnum;
import jakarta.validation.constraints.NotNull;

public record ImagemUpdateRequest(
        @NotNull(message = "Tipo de armazenamento é obrigatório") TipoArmazenamentoEnum tipoArmazenamento) {
}
