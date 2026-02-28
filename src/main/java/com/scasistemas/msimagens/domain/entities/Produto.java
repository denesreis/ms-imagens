package com.scasistemas.msimagens.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidade de domínio que representa um produto do catálogo.
 *
 * <p>
 * Cada produto pode ter múltiplas imagens associadas.
 * A identificação principal para consulta pública é o {@code codigoEan}.
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Produto {

    /** UUID gerado pela aplicação (não auto-incremento). */
    private String id;

    /**
     * Descrição resumida do produto.
     * Máximo 80 caracteres.
     */
    private String descricao;

    /**
     * Descrição detalhada / discriminação do produto.
     * Máximo 200 caracteres.
     */
    private String discriminacao;

    /**
     * Código do produto no sistema ERP externo.
     * Máximo 20 caracteres.
     */
    private String codigoErp;

    /** ID da empresa proprietária. Null = produto compartilhado. */
    private Long idEmpresa;

    /**
     * Código de barras EAN-13 do produto.
     * Máximo 13 caracteres.
     */
    private String codigoEan;

    /** Indica se o produto está ativo. False = soft delete. */
    @Builder.Default
    private Boolean ativo = true;

    private LocalDateTime dataCriacao;
    private LocalDateTime dataAtualizacao;
}
