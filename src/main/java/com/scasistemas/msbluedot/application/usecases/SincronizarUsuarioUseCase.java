package com.scasistemas.msbluedot.application.usecases;

import com.scasistemas.msbluedot.application.dto.sync.SincronizarUsuarioRequest;
import com.scasistemas.msbluedot.application.dto.usuario.UsuarioResponse;
import com.scasistemas.msbluedot.application.mappers.UsuarioMapper;
import com.scasistemas.msbluedot.domain.entities.Usuario;
import com.scasistemas.msbluedot.domain.enums.RoleEnum;
import com.scasistemas.msbluedot.domain.exceptions.ResourceNotFoundException;
import com.scasistemas.msbluedot.domain.repositories.IEmpresaRepository;
import com.scasistemas.msbluedot.domain.repositories.IUsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use Case para sincronização (upsert) de usuário MASTER vindo do
 * novo-mercador.
 *
 * <p>
 * Regras:
 * <ul>
 * <li>Se o usuário com o {@code nome} informado não existir → cria com role
 * {@code ADMINISTRADOR}</li>
 * <li>Se já existir → atualiza a senha e o idEmpresa</li>
 * <li>A proteção de acesso é feita pelo {@code SyncApiKeyFilter}, não
 * por @PreAuthorize</li>
 * </ul>
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SincronizarUsuarioUseCase {

    private final IEmpresaRepository empresaRepository;
    private final IUsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UsuarioResponse execute(SincronizarUsuarioRequest request) {
        log.info("[SincronizarUsuario] Upsert de usuário nome='{}'", request.getNome());

        Long empresaId = empresaRepository.findByCodigoErp(String.valueOf(request.getIdEmpresa()))
                .map(empresa -> {
                    log.info("[SincronizarUsuario] Empresa codigoErp='{}' resolvida para id={}",
                            request.getIdEmpresa(), empresa.getId());
                    return empresa.getId();
                })
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Empresa não encontrada para codigoErp: " + request.getIdEmpresa()));

        Usuario usuario = usuarioRepository.findByNomeIncludingInactive(request.getNome())
                .map(existente -> {
                    log.info(
                            "[SincronizarUsuario] Usuário existente id={} ativo={} — atualizando senha, empresa e role",
                            existente.getId(), existente.getAtivo());
                    existente.setSenha(passwordEncoder.encode(request.getSenha()));
                    existente.setIdEmpresa(empresaId);
                    existente.setRole(mapRole(request.getRole()));
                    existente.setAtivo(true);
                    existente.setTentativasLogin(0);
                    existente.setBloqueadoAte(null);
                    return existente;
                })
                .orElseGet(() -> {
                    log.info("[SincronizarUsuario] Usuário não encontrado — criando novo");
                    return Usuario.builder()
                            .nome(request.getNome())
                            .senha(passwordEncoder.encode(request.getSenha()))
                            .idEmpresa(empresaId)
                            .role(mapRole(request.getRole()))
                            .ativo(true)
                            .tentativasLogin(0)
                            .build();
                });

        Usuario salvo = usuarioRepository.save(usuario);
        log.info("[SincronizarUsuario] Usuário id={} sincronizado com sucesso", salvo.getId());
        return usuarioMapper.toResponse(salvo);
    }

    /**
     * Mapeia a role do novo-mercador para o RoleEnum do ms-bluedot.
     * MASTER e ADMINISTRADOR → ADMINISTRADOR; USUARIO → USUARIO.
     */
    private RoleEnum mapRole(String role) {
        if ("USUARIO".equalsIgnoreCase(role)) {
            return RoleEnum.USUARIO;
        }
        return RoleEnum.ADMINISTRADOR;
    }
}
