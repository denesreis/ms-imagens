package com.scasistemas.msbluedot.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados para sincronização de usuário externo")
public class SincronizarUsuarioRequest {

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 30, message = "Nome deve ter no máximo 30 caracteres")
    @Schema(description = "Username (mesmo do novo-mercador)", example = "joao.silva")
    private String nome;

    @NotBlank(message = "Senha é obrigatória")
    @Schema(description = "Senha em texto claro — será hasheada no armazenamento")
    private String senha;

    @NotNull(message = "ID da empresa é obrigatório")
    @Schema(description = "ID da empresa no novo-mercador", example = "4")
    private Long idEmpresa;

    @Schema(description = "Role do usuário no novo-mercador (MASTER, ADMINISTRADOR, USUARIO)", example = "ADMINISTRADOR")
    private String role;
}
