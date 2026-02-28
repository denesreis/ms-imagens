package com.scasistemas.msimagens.infrastructure.persistence.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Entidade JPA que representa um registro de auditoria na tabela
 * {@code tb_audit_log}.
 *
 * <p>
 * Registros de auditoria são <strong>imutáveis</strong> — criados uma vez
 * e nunca alterados. Não possui {@code @LastModifiedDate}.
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tb_audit_log", indexes = {
        @Index(name = "idx_audit_log_acao", columnList = "acao"),
        @Index(name = "idx_audit_log_usuario", columnList = "usuario"),
        @Index(name = "idx_audit_log_data_criacao", columnList = "data_criacao"),
        @Index(name = "idx_audit_log_id_empresa", columnList = "id_empresa")
})
@EntityListeners(AuditingEntityListener.class)
public class AuditLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String acao;

    @Column(nullable = false, length = 30)
    private String usuario;

    @Column(name = "id_empresa")
    private Long idEmpresa;

    @Column(length = 45)
    private String ip;

    @Column(columnDefinition = "TEXT")
    private String detalhes;

    @CreatedDate
    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;
}
