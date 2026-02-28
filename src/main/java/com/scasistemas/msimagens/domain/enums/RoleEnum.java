package com.scasistemas.msimagens.domain.enums;

/**
 * Perfis de acesso dos usuários do sistema.
 */
public enum RoleEnum {

    /**
     * Acesso total: gerencia empresas, usuários, produtos e imagens.
     */
    ADMINISTRADOR,

    /**
     * Acesso restrito: gerencia apenas produtos e imagens da própria empresa.
     */
    USUARIO
}
