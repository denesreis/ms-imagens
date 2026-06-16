package com.scasistemas.msbluedot.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados para criação ou atualização de produto")
public class ProdutoRequest {

    @Size(max = 80, message = "Descrição deve ter no máximo 80 caracteres")
    @Schema(description = "Descrição resumida do produto", example = "Caneta Azul BIC")
    private String descricao;

    @Size(max = 200, message = "Discriminação deve ter no máximo 200 caracteres")
    @Schema(description = "Descrição detalhada", example = "Caneta esferográfica azul, ponta 1.0mm")
    private String discriminacao;

    @Size(max = 20, message = "Código ERP deve ter no máximo 20 caracteres")
    @Schema(description = "Código no sistema ERP", example = "PROD-001")
    private String codigoErp;

    @Size(max = 13, message = "EAN deve ter no máximo 13 caracteres")
    @Schema(description = "Código de barras EAN-13", example = "7891234567890")
    private String codigoEan;

    @Schema(description = "ID da empresa proprietária (opcional para ADMIN)", example = "1")
    private Long idEmpresa;
}
