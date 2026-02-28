package com.scasistemas.msimagens.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidade de domínio que representa uma empresa cadastrada no sistema.
 *
 * <p>
 * Cada empresa possui um código de integração com o ERP ({@code codigoErp})
 * e agrupa usuários, produtos e imagens.
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Empresa {

    private Long id;

    /**
     * Código de identificação da empresa no sistema ERP externo.
     * Máximo 20 caracteres.
     */
    private String codigoErp;

    /**
     * Razão social ou nome fantasia da empresa.
     * Máximo 100 caracteres.
     */
    private String nome;

    /** Indica se a empresa está ativa. False = soft delete. */
    @Builder.Default
    private Boolean ativo = true;

    private LocalDateTime dataCriacao;
    private LocalDateTime dataAtualizacao;
}
