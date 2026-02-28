package com.scasistemas.msimagens.domain.repositories;

import com.scasistemas.msimagens.domain.entities.AuditLog;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Contrato de repositório para a entidade {@link AuditLog}.
 *
 * <p>
 * Interface de domínio puro — sem dependência de frameworks.
 * A implementação está na camada de infraestrutura.
 * </p>
 *
 * <p>
 * <strong>Imutabilidade:</strong> registros de auditoria não podem ser
 * editados nem excluídos — apenas criados e consultados.
 * </p>
 */
public interface IAuditLogRepository {

    /**
     * Persiste um novo registro de auditoria.
     * Não existe update — registros de auditoria são imutáveis.
     */
    AuditLog save(AuditLog auditLog);

    /**
     * Busca registros de auditoria de um usuário específico.
     *
     * @param usuario nome do usuário
     */
    List<AuditLog> findByUsuario(String usuario);

    /**
     * Busca registros de auditoria por tipo de ação.
     *
     * @param acao código da ação (ex: LOGIN, LOGOUT, LOGIN_FALHA)
     */
    List<AuditLog> findByAcao(String acao);

    /**
     * Busca registros de auditoria em um intervalo de datas.
     *
     * @param inicio início do período (inclusive)
     * @param fim    fim do período (inclusive)
     */
    List<AuditLog> findByDateRange(LocalDateTime inicio, LocalDateTime fim);

    /**
     * Busca registros de auditoria por empresa.
     *
     * @param idEmpresa ID da empresa
     */
    List<AuditLog> findByIdEmpresa(Long idEmpresa);
}
