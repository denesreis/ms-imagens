package com.scasistemas.msbluedot.dto;

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
public class LoginRequest {

    @NotBlank(message = "Nome de usuário é obrigatório")
    @Size(max = 30, message = "Nome de usuário deve ter no máximo 30 caracteres")
    private String nome;

    @NotBlank(message = "Senha é obrigatória")
    private String senha;
}
