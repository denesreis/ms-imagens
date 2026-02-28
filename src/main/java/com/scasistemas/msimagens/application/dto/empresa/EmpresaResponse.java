package com.scasistemas.msimagens.application.dto.empresa;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO de resposta de empresa.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados de empresa retornados pela API")
public class EmpresaResponse {

    @Schema(description = "ID da empresa", example = "1")
    private Long id;

    @Schema(description = "Código de integração com o ERP", example = "EMP001")
    private String codigoErp;

    @Schema(description = "Nome da empresa", example = "Empresa Exemplo Ltda")
    private String nome;

    @Schema(description = "Indica se a empresa está ativa")
    private Boolean ativo;

    @Schema(description = "Data de criação")
    private LocalDateTime dataCriacao;

    @Schema(description = "Data da última atualização")
    private LocalDateTime dataAtualizacao;
}
