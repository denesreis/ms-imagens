package com.scasistemas.msimagens.application.usecases;

import com.scasistemas.msimagens.application.dto.usuario.UsuarioRequest;
import com.scasistemas.msimagens.application.dto.usuario.UsuarioResponse;
import com.scasistemas.msimagens.application.mappers.UsuarioMapper;
import com.scasistemas.msimagens.domain.entities.AuditLog;
import com.scasistemas.msimagens.domain.entities.Usuario;
import com.scasistemas.msimagens.domain.exceptions.ResourceNotFoundException;
import com.scasistemas.msimagens.domain.exceptions.UnauthorizedException;
import com.scasistemas.msimagens.domain.repositories.IAuditLogRepository;
import com.scasistemas.msimagens.domain.repositories.IUsuarioRepository;
import com.scasistemas.msimagens.infrastructure.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Use Case para atualização de usuário.
 * ADMIN pode atualizar qualquer usuário; USUARIO pode atualizar apenas seus
 * próprios dados.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateUsuarioUseCase {

    private final IUsuarioRepository usuarioRepository;
    private final IAuditLogRepository auditLogRepository;
    private final UsuarioMapper usuarioMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UsuarioResponse execute(Long id, UsuarioRequest request) {
        log.info("[UpdateUsuario] Atualizando usuário id={}", id);

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + id));

        // USUÁRIO só pode atualizar seus próprios dados (ADMIN pode atualizar qualquer
        // um)
        if (!SecurityUtils.isAdministrador()) {
            String currentUsername = SecurityUtils.getCurrentUsername();
            if (!usuario.getNome().equals(currentUsername)) {
                throw new UnauthorizedException("Acesso negado: você só pode atualizar seus próprios dados");
            }
        }

        // Atualizar campos (role e idEmpresa apenas ADMIN pode alterar)
        if (SecurityUtils.isAdministrador()) {
            usuario.setRole(request.getRole());
            usuario.setIdEmpresa(request.getIdEmpresa());
        }

        // Atualizar senha se fornecida
        if (StringUtils.hasText(request.getSenha())) {
            usuario.setSenha(passwordEncoder.encode(request.getSenha()));
        }

        Usuario atualizado = usuarioRepository.save(usuario);

        auditLogRepository.save(AuditLog.builder()
                .acao("ATUALIZAR_USUARIO")
                .usuario(SecurityUtils.getCurrentUsername())
                .idEmpresa(atualizado.getIdEmpresa())
                .detalhes("Usuário atualizado: " + atualizado.getNome())
                .build());

        log.info("[UpdateUsuario] Usuário id={} atualizado com sucesso", id);
        return usuarioMapper.toResponse(atualizado);
    }
}
