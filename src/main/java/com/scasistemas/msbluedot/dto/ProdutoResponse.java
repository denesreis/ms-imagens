package com.scasistemas.msbluedot.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados de produto retornados pela API")
public class ProdutoResponse {

    @Schema(description = "ID do produto (UUID)", example = "550e8400-e29b-41d4-a716-446655440000")
    private String id;

    @Schema(description = "Descrição resumida", example = "Caneta Azul BIC")
    private String descricao;

    @Schema(description = "Descrição detalhada")
    private String discriminacao;

    @Schema(description = "Código no ERP", example = "PROD-001")
    private String codigoErp;

    @Schema(description = "Código de barras EAN-13", example = "7891234567890")
    private String codigoEan;

    @Schema(description = "ID da empresa proprietária")
    private Long idEmpresa;

    @Schema(description = "Indica se o produto está ativo")
    private Boolean ativo;

    @Schema(description = "Data de criação")
    private LocalDateTime dataCriacao;

    @Schema(description = "Data da última atualização")
    private LocalDateTime dataAtualizacao;

    @Schema(description = "Imagens do produto — populado apenas em criação/atualização com upload")
    private List<ImagemResponse> imagens;
}
