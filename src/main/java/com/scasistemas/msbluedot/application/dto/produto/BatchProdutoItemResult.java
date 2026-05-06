package com.scasistemas.msbluedot.application.dto.produto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * Resultado individual do processamento em lote de um produto.
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Resultado do processamento de um item do lote")
public class BatchProdutoItemResult {

    @Schema(description = "Código EAN do produto processado", example = "7891234567890")
    private String codigoEan;

    @Schema(description = "Status do processamento", allowableValues = { "CRIADO", "ERRO", "IGNORADO" })
    private String status;

    @Schema(description = "Mensagem descritiva do resultado")
    private String mensagem;

    @Schema(description = "Indica se imagem foi encontrada na API eanpictures.com.br")
    private boolean imagemEncontrada;

    @Schema(description = "Indica se a imagem foi enviada com sucesso para a Cloudflare")
    private boolean imagemCarregada;

    @Schema(description = "Produto criado (null em caso de erro)")
    private ProdutoResponse produto;
}

