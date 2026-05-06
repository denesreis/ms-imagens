package com.scasistemas.msbluedot.application.dto.imagem;

import com.scasistemas.msbluedot.domain.enums.TipoArmazenamentoEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para upload de imagem (usado em conjunto com MultipartFile).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Metadados para upload de imagem (enviado junto com o arquivo)")
public class ImagemUploadRequest {

    @NotBlank(message = "ID do produto é obrigatório")
    @Schema(description = "UUID do produto ao qual a imagem pertence", example = "550e8400-e29b-41d4-a716-446655440000")
    private String idProduto;

    @NotNull(message = "Tipo de armazenamento é obrigatório")
    @Schema(description = "ABERTO = público, PRIVADO = requer URL assinada")
    private TipoArmazenamentoEnum tipoArmazenamento;

    @Schema(description = "ID da empresa (inferido do token se ausente)")
    private Long idEmpresa;
}

