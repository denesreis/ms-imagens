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
@Schema(description = "Dados para sincronização de empresa externa")
public class SincronizarEmpresaRequest {

    @NotBlank(message = "Código ERP é obrigatório")
    @Size(max = 20, message = "Código ERP deve ter no máximo 20 caracteres")
    @Schema(description = "ID da empresa no novo-mercador (usado como codigoErp)", example = "5")
    private String codigoErp;

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
    @Schema(description = "Nome/razão social da empresa", example = "Empresa Exemplo Ltda")
    private String nome;
}
