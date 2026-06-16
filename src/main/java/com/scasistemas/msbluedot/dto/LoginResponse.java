package com.scasistemas.msbluedot.dto;

import com.scasistemas.msbluedot.enums.RoleEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String accessToken;
    private String refreshToken;

    @Builder.Default
    private String tipo = "Bearer";

    private Long expiresIn;
    private Long refreshExpiresIn;
    private UsuarioInfo usuario;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UsuarioInfo {
        private Long id;
        private String nome;
        private RoleEnum role;
        private Long idEmpresa;
    }
}
