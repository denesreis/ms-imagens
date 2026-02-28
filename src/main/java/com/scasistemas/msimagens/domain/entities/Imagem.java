package com.scasistemas.msimagens.domain.entities;

import com.scasistemas.msimagens.domain.enums.StatusImagemEnum;
import com.scasistemas.msimagens.domain.enums.TipoArmazenamentoEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidade de domínio que representa uma imagem de produto.
 *
 * <p>
 * Ciclo de vida do upload:
 * </p>
 * <ol>
 * <li>Registro criado com {@code status = PENDENTE}</li>
 * <li>Upload enviado à Cloudflare Images API</li>
 * <li>Confirmação: {@code status = ATIVO} (sucesso) ou {@code status = ERRO}
 * (falha)</li>
 * </ol>
 *
 * <p>
 * Imagens com {@code tipoArmazenamento = ABERTO} são acessíveis publicamente
 * pelo endpoint GET /imagens/ean/{codigoEan} sem autenticação.
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Imagem {

    /** UUID gerado pela aplicação (não auto-incremento). */
    private String id;

    /** UUID do produto ao qual esta imagem pertence. */
    private String idProduto;

    /** ID da empresa proprietária. Null = imagem compartilhada. */
    private Long idEmpresa;

    /** Define se a imagem é pública (ABERTO) ou requer URL assinada (PRIVADO). */
    private TipoArmazenamentoEnum tipoArmazenamento;

    /**
     * Identificador da imagem retornado pela Cloudflare Images API.
     * Máximo 50 caracteres.
     */
    private String idImagemCloudflare;

    /**
     * URL pública ou base da imagem na Cloudflare.
     * Armazenado como TEXT no banco.
     */
    private String url;

    /** Nome original do arquivo enviado. */
    private String filename;

    /**
     * Status atual do processamento do upload.
     * Valor inicial: PENDENTE.
     */
    @Builder.Default
    private StatusImagemEnum status = StatusImagemEnum.PENDENTE;

    /** Indica se a imagem está ativa. False = soft delete. */
    @Builder.Default
    private Boolean ativo = true;

    private LocalDateTime dataCriacao;
    private LocalDateTime dataAtualizacao;

    // =====================================================================
    // Métodos de domínio
    // =====================================================================

    /** Marca o upload como bem-sucedido. */
    public void confirmarUpload(String idCloudflare, String urlCloudflare) {
        this.idImagemCloudflare = idCloudflare;
        this.url = urlCloudflare;
        this.status = StatusImagemEnum.ATIVO;
    }

    /** Marca o upload como falho. */
    public void marcarErro() {
        this.status = StatusImagemEnum.ERRO;
    }

    /** Verifica se a imagem está disponível para acesso. */
    public boolean estaDisponivel() {
        return StatusImagemEnum.ATIVO.equals(this.status) && Boolean.TRUE.equals(this.ativo);
    }
}
