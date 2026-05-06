package com.scasistemas.msbluedot.domain.entities;

import com.scasistemas.msbluedot.domain.enums.RoleEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidade de domínio que representa um usuário do sistema.
 *
 * <p>
 * O campo {@code nome} é utilizado como username para autenticação
 * e possui restrição UNIQUE no banco de dados.
 * </p>
 *
 * <p>
 * Proteção contra brute force: os campos {@code tentativasLogin} e
 * {@code bloqueadoAte} controlam o bloqueio temporário de conta.
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    private Long id;

    /** ID da empresa à qual o usuário pertence. */
    private Long idEmpresa;

    /**
     * Nome de usuário utilizado no login.
     * Máximo 30 caracteres. ÚNICO no sistema.
     */
    private String nome;

    /**
     * Senha armazenada como hash BCrypt.
     * Máximo 60 caracteres.
     */
    private String senha;

    /** Perfil de acesso do usuário. */
    private RoleEnum role;

    /** Indica se o usuário está ativo. False = soft delete. */
    @Builder.Default
    private Boolean ativo = true;

    /**
     * Contador de tentativas de login malsucedidas consecutivas.
     * Zerado ao fazer login com sucesso.
     */
    @Builder.Default
    private Integer tentativasLogin = 0;

    /**
     * Data/hora até quando o acesso está bloqueado.
     * Null quando a conta não está bloqueada.
     */
    private LocalDateTime bloqueadoAte;

    private LocalDateTime dataCriacao;
    private LocalDateTime dataAtualizacao;

    // =====================================================================
    // Métodos de domínio
    // =====================================================================

    /**
     * Verifica se a conta está bloqueada no momento atual.
     */
    public boolean estaBloqueado() {
        return bloqueadoAte != null && LocalDateTime.now().isBefore(bloqueadoAte);
    }

    /**
     * Registra uma tentativa de login malsucedida.
     *
     * @param maxTentativas   número máximo antes de bloquear
     * @param duracaoBloqueio duração do bloqueio em milissegundos
     */
    public void registrarFalhaLogin(int maxTentativas, long duracaoBloqueio) {
        this.tentativasLogin = (this.tentativasLogin == null ? 0 : this.tentativasLogin) + 1;
        if (this.tentativasLogin >= maxTentativas) {
            this.bloqueadoAte = LocalDateTime.now()
                    .plusNanos(duracaoBloqueio * 1_000_000L);
        }
    }

    /**
     * Zera o contador de falhas após um login bem-sucedido.
     */
    public void registrarLoginSucesso() {
        this.tentativasLogin = 0;
        this.bloqueadoAte = null;
    }
}

