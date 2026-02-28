package com.scasistemas.msimagens.infrastructure.persistence.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Entidade JPA que representa uma empresa na tabela {@code tb_empresa}.
 *
 * <p>
 * <strong>Soft delete:</strong> a exclusão marca {@code ativo = false}
 * e o {@code @SQLRestriction} filtra automaticamente todos os selects.
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tb_empresa", indexes = {
        @Index(name = "idx_empresa_codigo_erp", columnList = "codigo_erp", unique = true),
        @Index(name = "idx_empresa_ativo", columnList = "ativo")
})
@EntityListeners(AuditingEntityListener.class)
@SQLRestriction("ativo = true")
@SQLDelete(sql = "UPDATE tb_empresa SET ativo = false WHERE id = ?")
public class EmpresaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_erp", nullable = false, length = 20, unique = true)
    private String codigoErp;

    @Column(nullable = false, length = 100)
    private String nome;

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
