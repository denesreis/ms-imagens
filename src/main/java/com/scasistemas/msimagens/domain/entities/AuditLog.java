package com.scasistemas.msimagens.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidade de domínio que registra ações relevantes para auditoria.
 *
 * <p>
 * Exemplos de ações registradas:
 * </p>
 * <ul>
 * <li>{@code LOGIN} — login bem-sucedido</li>
 * <li>{@code LOGOUT} — logout explícito</li>
 * <li>{@code LOGIN_FALHA} — tentativa de login inválida</li>
 * <li>{@code CRIAR_USUARIO} — cadastro de usuário</li>
 * <li>{@code DELETAR_IMAGEM} — remoção de imagem</li>
 * <li>{@code UPLOAD_IMAGEM} — upload realizado</li>
 * </ul>
 *
 * <p>
 * Registros de auditoria são <strong>imutáveis</strong> — nunca devem ser
 * editados ou excluídos após criação.
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    private Long id;

    /**
     * Código da ação executada.
     * Máximo 50 caracteres. Ex: LOGIN, LOGOUT, CRIAR_PRODUTO.
     */
    private String acao;

    /**
     * Nome do usuário que executou a ação.
     * Máximo 30 caracteres.
     */
    private String usuario;

    /** ID da empresa relacionada à ação. Null = ação global. */
    private Long idEmpresa;

    /**
     * Endereço IP de origem da requisição.
     * Máximo 45 caracteres (suporta IPv4 e IPv6).
     */
    private String ip;

    /**
     * Informações adicionais sobre a ação (opcional).
     * Armazenado como TEXT no banco.
     */
    private String detalhes;

    /** Data/hora em que a ação ocorreu. */
    private LocalDateTime dataCriacao;
}
