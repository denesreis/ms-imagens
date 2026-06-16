package com.scasistemas.msbluedot.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados para criação ou atualização de empresa")
public class EmpresaRequest {

    @NotBlank(message = "Código ERP é obrigatório")
    @Size(max = 20, message = "Código ERP deve ter no máximo 20 caracteres")
    @Schema(description = "Código de integração com o ERP externo", example = "EMP001")
    private String codigoErp;

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
    @Schema(description = "Razão social ou nome fantasia da empresa", example = "Empresa Exemplo Ltda")
    private String nome;
}
