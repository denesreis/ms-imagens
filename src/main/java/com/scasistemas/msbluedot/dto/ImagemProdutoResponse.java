package com.scasistemas.msbluedot.dto;

import com.scasistemas.msbluedot.enums.StatusImagemEnum;
import com.scasistemas.msbluedot.enums.TipoArmazenamentoEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados simplificados de imagem para consultas por EAN/produto")
public class ImagemProdutoResponse {

    @Schema(description = "ID da imagem (UUID)")
    private String id;

    @Schema(description = "URL pública ou base da imagem")
    private String url;

    @Schema(description = "Nome original do arquivo")
    private String filename;

    @Schema(description = "Tipo de armazenamento")
    private TipoArmazenamentoEnum tipoArmazenamento;

    @Schema(description = "Status do upload")
    private StatusImagemEnum status;
}
