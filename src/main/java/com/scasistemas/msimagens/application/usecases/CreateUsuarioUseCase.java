package com.scasistemas.msimagens.application.usecases;

import com.scasistemas.msimagens.application.dto.usuario.UsuarioRequest;
import com.scasistemas.msimagens.application.dto.usuario.UsuarioResponse;
import com.scasistemas.msimagens.application.mappers.UsuarioMapper;
import com.scasistemas.msimagens.domain.entities.AuditLog;
import com.scasistemas.msimagens.domain.entities.Usuario;
import com.scasistemas.msimagens.domain.exceptions.DuplicateUsernameException;
import com.scasistemas.msimagens.domain.exceptions.ResourceNotFoundException;
import com.scasistemas.msimagens.domain.repositories.IAuditLogRepository;
import com.scasistemas.msimagens.domain.repositories.IEmpresaRepository;
import com.scasistemas.msimagens.domain.repositories.IUsuarioRepository;
import com.scasistemas.msimagens.infrastructure.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use Case para criação de usuário (apenas ADMINISTRADOR).
 *
 * <p>
 * Regras:
 * <ul>
 * <li>Somente ADMINISTRADOR pode criar usuários</li>
 * <li>Username (nome) deve ser único no sistema</li>
 * <li>Senha é criptografada com BCrypt antes de persistir</li>
 * <li>Empresa informada deve existir</li>
 * </ul>
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CreateUsuarioUseCase {

    private final IUsuarioRepository usuarioRepository;
    private final IEmpresaRepository empresaRepository;
    private final IAuditLogRepository auditLogRepository;
    private final UsuarioMapper usuarioMapper;
    private final PasswordEncoder passwordEncoder;

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Transactional
    public UsuarioResponse execute(UsuarioRequest request) {
        log.info("[CreateUsuario] Criando usuário nome='{}'", request.getNome());

        // Verificar duplicidade de username
        usuarioRepository.findByNome(request.getNome()).ifPresent(u -> {
            throw new DuplicateUsernameException("Já existe um usuário com o nome: " + request.getNome());
        });

        // Verificar se empresa existe
        if (!empresaRepository.existsById(request.getIdEmpresa())) {
            throw new ResourceNotFoundException("Empresa não encontrada: " + request.getIdEmpresa());
        }

        Usuario usuario = usuarioMapper.fromRequest(request);
        // Hash da senha com BCrypt (strength 12)
        usuario.setSenha(passwordEncoder.encode(request.getSenha()));

        Usuario salvo = usuarioRepository.save(usuario);

        auditLogRepository.save(AuditLog.builder()
                .acao("CRIAR_USUARIO")
                .usuario(SecurityUtils.getCurrentUsername())
                .idEmpresa(request.getIdEmpresa())
                .detalhes("Usuário criado: " + salvo.getNome() + " | role=" + salvo.getRole())
                .build());

        log.info("[CreateUsuario] Usuário id={} criado com sucesso", salvo.getId());
        return usuarioMapper.toResponse(salvo);
    }
}
