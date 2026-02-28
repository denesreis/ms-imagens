package com.scasistemas.msimagens.application.usecases;

import com.scasistemas.msimagens.application.dto.usuario.UsuarioResponse;
import com.scasistemas.msimagens.application.mappers.UsuarioMapper;
import com.scasistemas.msimagens.domain.entities.Usuario;
import com.scasistemas.msimagens.domain.exceptions.ResourceNotFoundException;
import com.scasistemas.msimagens.domain.exceptions.UnauthorizedException;
import com.scasistemas.msimagens.domain.repositories.IUsuarioRepository;
import com.scasistemas.msimagens.infrastructure.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use Case para busca de usuário por ID.
 * ADMIN pode ver qualquer usuário; USUARIO só pode ver da mesma empresa.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GetUsuarioUseCase {

    private final IUsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;

    @Transactional(readOnly = true)
    public UsuarioResponse execute(Long id) {
        log.debug("[GetUsuario] Buscando usuário id={}", id);

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + id));

        // USUÁRIO só pode ver usuários da mesma empresa
        if (!SecurityUtils.isAdministrador()) {
            Long idEmpresaToken = SecurityUtils.getCurrentIdEmpresa();
            if (!idEmpresaToken.equals(usuario.getIdEmpresa())) {
                throw new UnauthorizedException("Acesso negado ao usuário solicitado");
            }
        }

        return usuarioMapper.toResponse(usuario);
    }
}
