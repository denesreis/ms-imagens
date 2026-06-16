package com.scasistemas.msbluedot.service;

import com.scasistemas.msbluedot.dto.EmpresaResponse;
import com.scasistemas.msbluedot.dto.RefreshTokenResponse;
import com.scasistemas.msbluedot.dto.SincronizarEmpresaRequest;
import com.scasistemas.msbluedot.dto.SincronizarUsuarioRequest;
import com.scasistemas.msbluedot.dto.SyncTokenRequest;
import com.scasistemas.msbluedot.dto.UsuarioResponse;
import com.scasistemas.msbluedot.entity.Empresa;
import com.scasistemas.msbluedot.entity.Usuario;
import com.scasistemas.msbluedot.enums.RoleEnum;
import com.scasistemas.msbluedot.exception.ResourceNotFoundException;
import com.scasistemas.msbluedot.repository.EmpresaRepository;
import com.scasistemas.msbluedot.repository.UsuarioRepository;
import com.scasistemas.msbluedot.security.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class SyncService {

    private final EmpresaRepository empresaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthService authService;
    private final EmpresaService empresaService;
    private final UsuarioService usuarioService;

    public SyncService(
            EmpresaRepository empresaRepository,
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            @Qualifier("newJwtTokenProvider") JwtTokenProvider jwtTokenProvider,
            AuthService authService,
            EmpresaService empresaService,
            UsuarioService usuarioService) {
        this.empresaRepository = empresaRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authService = authService;
        this.empresaService = empresaService;
        this.usuarioService = usuarioService;
    }

    @Transactional
    public EmpresaResponse sincronizarEmpresa(SincronizarEmpresaRequest request) {
        log.info("[SincronizarEmpresa] Upsert de empresa codigoErp='{}'", request.getCodigoErp());

        Empresa empresa = empresaRepository.findByCodigoErp(request.getCodigoErp())
                .map(existente -> {
                    log.info("[SincronizarEmpresa] Empresa existente id={} — atualizando nome", existente.getId());
                    existente.setNome(request.getNome());
                    return existente;
                })
                .orElseGet(() -> {
                    log.info("[SincronizarEmpresa] Empresa não encontrada — criando nova");
                    return Empresa.builder()
                            .codigoErp(request.getCodigoErp())
                            .nome(request.getNome())
                            .ativo(true)
                            .build();
                });

        Empresa salva = empresaRepository.save(empresa);
        log.info("[SincronizarEmpresa] Empresa id={} sincronizada com sucesso", salva.getId());
        return empresaService.toResponse(salva);
    }

    @Transactional
    public UsuarioResponse sincronizarUsuario(SincronizarUsuarioRequest request) {
        log.info("[SincronizarUsuario] Upsert de usuário nome='{}'", request.getNome());

        Long empresaId = empresaRepository.findByCodigoErp(String.valueOf(request.getIdEmpresa()))
                .map(empresa -> {
                    log.info("[SincronizarUsuario] Empresa codigoErp='{}' resolvida para id={}",
                            request.getIdEmpresa(), empresa.getId());
                    return empresa.getId();
                })
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Empresa não encontrada para codigoErp: " + request.getIdEmpresa()));

        Usuario usuario = usuarioRepository.findByNomeIgnoringSoftDelete(request.getNome())
                .map(existente -> {
                    log.info("[SincronizarUsuario] Usuário existente id={} ativo={} — atualizando",
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
        return usuarioService.toResponse(salvo);
    }

    @Transactional
    public RefreshTokenResponse emitirTokens(SyncTokenRequest request) {
        log.info("[EmitirTokensSync] Gerando tokens para usuário nome='{}'", request.getNome());

        Usuario usuario = usuarioRepository.findByNomeIgnoreCase(request.getNome())
                .orElseThrow(() -> {
                    log.warn("[EmitirTokensSync] Usuário '{}' não encontrado", request.getNome());
                    return new UsernameNotFoundException("Usuário não encontrado: " + request.getNome());
                });

        String accessToken = jwtTokenProvider.generateAccessToken(usuario);
        String rawRefreshToken = authService.criarRefreshToken(usuario);

        log.info("[EmitirTokensSync] Tokens emitidos para usuário id={}", usuario.getId());

        return RefreshTokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(rawRefreshToken)
                .build();
    }

    private RoleEnum mapRole(String role) {
        if ("USUARIO".equalsIgnoreCase(role)) {
            return RoleEnum.USUARIO;
        }
        return RoleEnum.ADMINISTRADOR;
    }
}
