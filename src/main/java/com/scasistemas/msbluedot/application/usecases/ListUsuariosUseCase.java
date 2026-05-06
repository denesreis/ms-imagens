package com.scasistemas.msbluedot.application.usecases;

import com.scasistemas.msbluedot.application.dto.usuario.UsuarioResponse;
import com.scasistemas.msbluedot.application.mappers.UsuarioMapper;
import com.scasistemas.msbluedot.domain.entities.Usuario;
import com.scasistemas.msbluedot.domain.repositories.IUsuarioRepository;
import com.scasistemas.msbluedot.infrastructure.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Use Case para listagem de usuários com paginação.
 * ADMIN vê todos; USUARIO vê apenas usuários da sua empresa.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ListUsuariosUseCase {

    private final IUsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;

    @Transactional(readOnly = true)
    public Page<UsuarioResponse> execute(Pageable pageable) {
        log.debug("[ListUsuarios] Listando usuários page={} size={}", pageable.getPageNumber(), pageable.getPageSize());

        List<Usuario> todos;
        if (SecurityUtils.isAdministrador()) {
            todos = usuarioRepository.findAll();
        } else {
            Long idEmpresa = SecurityUtils.getCurrentIdEmpresa();
            todos = usuarioRepository.findByIdEmpresa(idEmpresa);
        }

        List<UsuarioResponse> responses = todos.stream()
                .map(usuarioMapper::toResponse)
                .toList();

        int total = responses.size();
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), total);

        List<UsuarioResponse> paginado = (start >= total) ? List.of() : responses.subList(start, end);
        return new PageImpl<>(paginado, pageable, total);
    }
}

