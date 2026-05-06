package com.scasistemas.msbluedot.application.usecases;

import com.scasistemas.msbluedot.domain.entities.AuditLog;
import com.scasistemas.msbluedot.domain.entities.Usuario;
import com.scasistemas.msbluedot.domain.exceptions.ResourceNotFoundException;
import com.scasistemas.msbluedot.domain.repositories.IAuditLogRepository;
import com.scasistemas.msbluedot.domain.repositories.IUsuarioRepository;
import com.scasistemas.msbluedot.infrastructure.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use Case para exclusão (soft delete) de usuário (apenas ADMINISTRADOR).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeleteUsuarioUseCase {

    private final IUsuarioRepository usuarioRepository;
    private final IAuditLogRepository auditLogRepository;

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Transactional
    public void execute(Long id) {
        log.info("[DeleteUsuario] Soft delete usuário id={}", id);

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + id));

        usuarioRepository.deleteById(id);

        auditLogRepository.save(AuditLog.builder()
                .acao("DELETAR_USUARIO")
                .usuario(SecurityUtils.getCurrentUsername())
                .idEmpresa(usuario.getIdEmpresa())
                .detalhes("Usuário desativado: " + usuario.getNome())
                .build());

        log.info("[DeleteUsuario] Usuário id={} desativado com sucesso", id);
    }
}

