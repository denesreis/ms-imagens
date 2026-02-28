package com.scasistemas.msimagens.application.dto.usuario;

import com.scasistemas.msimagens.domain.enums.RoleEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para criação de usuário (apenas ADMINISTRADOR pode criar).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados para criação de usuário")
public class UsuarioRequest {

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 30, message = "Nome deve ter no máximo 30 caracteres")
    @Schema(description = "Username para login", example = "joao.silva")
    private String nome;

    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 8, max = 100, message = "Senha deve ter entre 8 e 100 caracteres")
    @Schema(description = "Senha (mínimo 8 caracteres)", example = "Senha@2024")
    private String senha;

    @NotNull(message = "Role é obrigatória")
    @Schema(description = "Perfil de acesso do usuário")
    private RoleEnum role;

    @NotNull(message = "ID da empresa é obrigatório")
    @Schema(description = "ID da empresa à qual o usuário pertence", example = "1")
    private Long idEmpresa;
}
