package com.scasistemas.msimagens.application.dto.usuario;

import com.scasistemas.msimagens.domain.enums.RoleEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO de resposta de usuário (sem senha).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados de usuário retornados pela API (sem senha)")
public class UsuarioResponse {

    @Schema(description = "ID do usuário", example = "10")
    private Long id;

    @Schema(description = "Nome / username", example = "joao.silva")
    private String nome;

    @Schema(description = "Perfil de acesso")
    private RoleEnum role;

    @Schema(description = "ID da empresa", example = "1")
    private Long idEmpresa;

    @Schema(description = "Indica se o usuário está ativo")
    private Boolean ativo;

    @Schema(description = "Data de criação")
    private LocalDateTime dataCriacao;

    @Schema(description = "Data da última atualização")
    private LocalDateTime dataAtualizacao;
}
