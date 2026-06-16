package com.scasistemas.msbluedot.entity;

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
@Table(name = "tb_produto", indexes = {
        @Index(name = "idx_produto_codigo_ean", columnList = "codigo_ean"),
        @Index(name = "idx_produto_codigo_erp", columnList = "codigo_erp"),
        @Index(name = "idx_produto_id_empresa", columnList = "id_empresa"),
        @Index(name = "idx_produto_ativo", columnList = "ativo")
})
@EntityListeners(AuditingEntityListener.class)
@SQLRestriction("ativo = true")
@SQLDelete(sql = "UPDATE tb_produto SET ativo = false WHERE id = ?")
public class Produto {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(length = 36, updatable = false, nullable = false)
    private String id;

    @Column(nullable = false, length = 80)
    private String descricao;

    @Column(length = 200)
    private String discriminacao;

    @Column(name = "codigo_erp", length = 20)
    private String codigoErp;

    @Column(name = "id_empresa")
    private Long idEmpresa;

    @Column(name = "codigo_ean", nullable = false, length = 13)
    private String codigoEan;

    @Builder.Default
    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean ativo = true;

    @CreatedDate
    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @LastModifiedDate
    @Column(name = "data_atualizacao", nullable = false)
    private LocalDateTime dataAtualizacao;
}
