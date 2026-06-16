package com.scasistemas.msbluedot.service;

import com.scasistemas.msbluedot.dto.UsuarioRequest;
import com.scasistemas.msbluedot.dto.UsuarioResponse;
import com.scasistemas.msbluedot.entity.AuditLog;
import com.scasistemas.msbluedot.entity.Usuario;
import com.scasistemas.msbluedot.exception.DuplicateUsernameException;
import com.scasistemas.msbluedot.exception.ResourceNotFoundException;
import com.scasistemas.msbluedot.exception.UnauthorizedException;
import com.scasistemas.msbluedot.repository.AuditLogRepository;
import com.scasistemas.msbluedot.repository.EmpresaRepository;
import com.scasistemas.msbluedot.repository.UsuarioRepository;
import com.scasistemas.msbluedot.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final EmpresaRepository empresaRepository;
    private final AuditLogRepository auditLogRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UsuarioResponse criar(UsuarioRequest request) {
        log.info("[CreateUsuario] Criando usuário nome='{}'", request.getNome());

        usuarioRepository.findByNomeIgnoreCase(request.getNome()).ifPresent(u -> {
            throw new DuplicateUsernameException("Já existe um usuário com o nome: " + request.getNome());
        });

        if (!empresaRepository.existsById(request.getIdEmpresa())) {
            throw new ResourceNotFoundException("Empresa não encontrada: " + request.getIdEmpresa());
        }

        Usuario usuario = Usuario.builder()
                .nome(request.getNome())
                .senha(passwordEncoder.encode(request.getSenha()))
                .role(request.getRole())
                .idEmpresa(request.getIdEmpresa())
                .ativo(true)
                .tentativasLogin(0)
                .build();

        Usuario salvo = usuarioRepository.save(usuario);

        auditLogRepository.save(AuditLog.builder()
                .acao("CRIAR_USUARIO")
                .usuario(SecurityUtils.getCurrentUsername())
                .idEmpresa(request.getIdEmpresa())
                .detalhes("Usuário criado: " + salvo.getNome() + " | role=" + salvo.getRole())
                .build());

        log.info("[CreateUsuario] Usuário id={} criado com sucesso", salvo.getId());
        return toResponse(salvo);
    }

    @Transactional
    public UsuarioResponse atualizar(Long id, UsuarioRequest request) {
        log.info("[UpdateUsuario] Atualizando usuário id={}", id);

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + id));

        if (!SecurityUtils.isAdministrador()) {
            String currentUsername = SecurityUtils.getCurrentUsername();
            if (!usuario.getNome().equals(currentUsername)) {
                throw new UnauthorizedException("Acesso negado: você só pode atualizar seus próprios dados");
            }
        }

        if (SecurityUtils.isAdministrador()) {
            usuario.setRole(request.getRole());
            usuario.setIdEmpresa(request.getIdEmpresa());
        }

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
        return toResponse(atualizado);
    }

    @Transactional(readOnly = true)
    public UsuarioResponse buscarPorId(Long id) {
        log.debug("[GetUsuario] Buscando usuário id={}", id);

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + id));

        if (!SecurityUtils.isAdministrador()) {
            Long idEmpresaToken = SecurityUtils.getCurrentIdEmpresa();
            if (!idEmpresaToken.equals(usuario.getIdEmpresa())) {
                throw new UnauthorizedException("Acesso negado ao usuário solicitado");
            }
        }

        return toResponse(usuario);
    }

    @Transactional(readOnly = true)
    public Page<UsuarioResponse> listar(Pageable pageable) {
        log.debug("[ListUsuarios] Listando usuários page={} size={}", pageable.getPageNumber(), pageable.getPageSize());

        if (SecurityUtils.isAdministrador()) {
            List<Usuario> todos = usuarioRepository.findAll();
            List<UsuarioResponse> responses = todos.stream().map(this::toResponse).toList();
            return paginar(responses, pageable);
        } else {
            Long idEmpresa = SecurityUtils.getCurrentIdEmpresa();
            Page<Usuario> page = usuarioRepository.findByIdEmpresa(idEmpresa,
                    PageRequest.of(pageable.getPageNumber(), pageable.getPageSize()));
            return page.map(this::toResponse);
        }
    }

    @Transactional
    public void deletar(Long id) {
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

    public UsuarioResponse toResponse(Usuario u) {
        return UsuarioResponse.builder()
                .id(u.getId())
                .nome(u.getNome())
                .role(u.getRole())
                .idEmpresa(u.getIdEmpresa())
                .ativo(u.getAtivo())
                .dataCriacao(u.getDataCriacao())
                .dataAtualizacao(u.getDataAtualizacao())
                .build();
    }

    private <T> Page<T> paginar(List<T> lista, Pageable pageable) {
        int total = lista.size();
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), total);
        List<T> paginado = (start >= total) ? List.of() : lista.subList(start, end);
        return new PageImpl<>(paginado, pageable, total);
    }
}
