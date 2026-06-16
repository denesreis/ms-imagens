package com.scasistemas.msbluedot.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "Resultado consolidado do processamento em lote")
public class BatchProdutoResponse {

    @Schema(description = "Total de itens recebidos na requisição")
    private int total;

    @Schema(description = "Quantidade de produtos criados com sucesso")
    private int criados;

    @Schema(description = "Quantidade de itens com erro")
    private int erros;

    @Schema(description = "Quantidade de itens ignorados (EAN duplicado, sem ação)")
    private int ignorados;

    @Schema(description = "Quantidade de itens sem imagem no eanpictures.com.br (produto NÃO cadastrado)")
    private int semImagem;

    @Schema(description = "Detalhes individuais de cada item processado")
    private List<BatchProdutoItemResult> resultados;
}
