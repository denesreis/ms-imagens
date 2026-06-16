package com.scasistemas.msbluedot.entity;

import com.scasistemas.msbluedot.enums.RoleEnum;
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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tb_usuario", uniqueConstraints = @UniqueConstraint(name = "uq_usuario_nome", columnNames = "nome"), indexes = {
        @Index(name = "idx_usuario_nome", columnList = "nome"),
        @Index(name = "idx_usuario_id_empresa", columnList = "id_empresa"),
        @Index(name = "idx_usuario_ativo", columnList = "ativo")
})
@EntityListeners(AuditingEntityListener.class)
@SQLRestriction("ativo = true")
@SQLDelete(sql = "UPDATE tb_usuario SET ativo = false WHERE id = ?")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_empresa")
    private Long idEmpresa;

    @Column(nullable = false, length = 30, unique = true)
    private String nome;

    @Column(nullable = false, length = 60)
    private String senha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RoleEnum role;

    @Builder.Default
    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean ativo = true;

    @Builder.Default
    @Column(name = "tentativas_login", nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    private Integer tentativasLogin = 0;

    @Column(name = "bloqueado_ate")
    private LocalDateTime bloqueadoAte;

    @CreatedDate
    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @LastModifiedDate
    @Column(name = "data_atualizacao", nullable = false)
    private LocalDateTime dataAtualizacao;

    public boolean estaBloqueado() {
        return bloqueadoAte != null && LocalDateTime.now().isBefore(bloqueadoAte);
    }

    public void registrarFalhaLogin(int maxTentativas, long duracaoBloqueio) {
        this.tentativasLogin = (this.tentativasLogin == null ? 0 : this.tentativasLogin) + 1;
        if (this.tentativasLogin >= maxTentativas) {
            this.bloqueadoAte = LocalDateTime.now().plusNanos(duracaoBloqueio * 1_000_000L);
        }
    }

    public void registrarLoginSucesso() {
        this.tentativasLogin = 0;
        this.bloqueadoAte = null;
    }
}
