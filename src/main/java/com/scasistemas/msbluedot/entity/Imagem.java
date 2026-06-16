package com.scasistemas.msbluedot.entity;

import com.scasistemas.msbluedot.enums.StatusImagemEnum;
import com.scasistemas.msbluedot.enums.TipoArmazenamentoEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tb_imagem", indexes = {
        @Index(name = "idx_imagem_id_produto", columnList = "id_produto"),
        @Index(name = "idx_imagem_id_empresa", columnList = "id_empresa"),
        @Index(name = "idx_imagem_status", columnList = "status"),
        @Index(name = "idx_imagem_tipo_armazenamento", columnList = "tipo_armazenamento"),
        @Index(name = "idx_imagem_ativo", columnList = "ativo")
})
@EntityListeners(AuditingEntityListener.class)
@SQLRestriction("ativo = true")
@SQLDelete(sql = "UPDATE tb_imagem SET ativo = false WHERE id = ?")
public class Imagem {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(length = 36, updatable = false, nullable = false)
    private String id;

    @Column(name = "id_produto", nullable = false, length = 36)
    private String idProduto;

    @Column(name = "id_empresa")
    private Long idEmpresa;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_armazenamento", nullable = false, length = 10)
    private TipoArmazenamentoEnum tipoArmazenamento;

    @Column(name = "id_imagem_cloudflare", length = 50)
    private String idImagemCloudflare;

    @Column(columnDefinition = "TEXT")
    private String url;

    @Column(length = 255)
    private String filename;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10, columnDefinition = "VARCHAR(10) DEFAULT 'PENDENTE'")
    private StatusImagemEnum status = StatusImagemEnum.PENDENTE;

    @Builder.Default
    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean ativo = true;

    @CreatedDate
    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @LastModifiedDate
    @Column(name = "data_atualizacao", nullable = false)
    private LocalDateTime dataAtualizacao;

    public void confirmarUpload(String idCloudflare, String urlCloudflare) {
        this.idImagemCloudflare = idCloudflare;
        this.url = urlCloudflare;
        this.status = StatusImagemEnum.ATIVO;
    }

    public void marcarErro() {
        this.status = StatusImagemEnum.ERRO;
    }

    public boolean estaDisponivel() {
        return StatusImagemEnum.ATIVO.equals(this.status) && Boolean.TRUE.equals(this.ativo);
    }
}
