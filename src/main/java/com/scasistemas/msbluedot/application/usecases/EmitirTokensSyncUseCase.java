package com.scasistemas.msbluedot.application.usecases;

import com.scasistemas.msbluedot.application.dto.auth.RefreshTokenResponse;
import com.scasistemas.msbluedot.application.dto.sync.SyncTokenRequest;
import com.scasistemas.msbluedot.application.services.RefreshTokenService;
import com.scasistemas.msbluedot.domain.entities.Usuario;
import com.scasistemas.msbluedot.domain.repositories.IUsuarioRepository;
import com.scasistemas.msbluedot.infrastructure.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use Case para emissão de tokens via sincronização interna (sem validação de
 * senha).
 *
 * <p>
 * Utilizado pelo novo-mercador para obter tokens Bluedot do usuário corrente
 * sem necessitar re-informar credenciais. A proteção de acesso é garantida
 * pelo {@code SyncApiKeyFilter} (header X-Sync-Key).
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmitirTokensSyncUseCase {

    private final IUsuarioRepository usuarioRepository;
    private final RefreshTokenService refreshTokenService;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public RefreshTokenResponse execute(SyncTokenRequest request) {
        log.info("[EmitirTokensSync] Gerando tokens para usuário nome='{}'", request.getNome());

        Usuario usuario = usuarioRepository.findByNome(request.getNome())
                .orElseThrow(() -> {
                    log.warn("[EmitirTokensSync] Usuário '{}' não encontrado", request.getNome());
                    return new UsernameNotFoundException("Usuário não encontrado: " + request.getNome());
                });

        String accessToken = jwtTokenProvider.generateAccessToken(usuario);
        String rawRefreshToken = refreshTokenService.createRefreshToken(usuario);

        log.info("[EmitirTokensSync] Tokens emitidos para usuário id={}", usuario.getId());

        return RefreshTokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(rawRefreshToken)
                .build();
    }
}
