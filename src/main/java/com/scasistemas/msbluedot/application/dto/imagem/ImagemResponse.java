package com.scasistemas.msbluedot.application.dto.imagem;

import com.scasistemas.msbluedot.domain.enums.StatusImagemEnum;
import com.scasistemas.msbluedot.domain.enums.TipoArmazenamentoEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO de resposta completa de imagem.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados de imagem retornados pela API")
public class ImagemResponse {

    @Schema(description = "ID da imagem (UUID)")
    private String id;

    @Schema(description = "UUID do produto associado")
    private String idProduto;

    @Schema(description = "ID da empresa proprietária")
    private Long idEmpresa;

    @Schema(description = "Tipo de armazenamento: ABERTO (público) ou PRIVADO")
    private TipoArmazenamentoEnum tipoArmazenamento;

    @Schema(description = "ID da imagem na Cloudflare")
    private String idImagemCloudflare;

    @Schema(description = "URL da imagem na Cloudflare")
    private String url;

    @Schema(description = "Nome original do arquivo")
    private String filename;

    @Schema(description = "Status do upload: PENDENTE, ATIVO ou ERRO")
    private StatusImagemEnum status;

    @Schema(description = "Indica se a imagem está ativa")
    private Boolean ativo;

    @Schema(description = "Data de criação")
    private LocalDateTime dataCriacao;

    @Schema(description = "Data da última atualização")
    private LocalDateTime dataAtualizacao;
}

