package com.scasistemas.msbluedot.application.usecases;

import com.scasistemas.msbluedot.application.dto.auth.RefreshTokenRequest;
import com.scasistemas.msbluedot.application.dto.auth.RefreshTokenResponse;
import com.scasistemas.msbluedot.application.services.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use Case de renovação de access token via refresh token.
 *
 * <p>
 * Aplica rotação de refresh token: o token antigo é revogado e
 * um novo par de tokens é gerado.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenUseCase {

    private final RefreshTokenService refreshTokenService;

    /**
     * Renova o access token usando um refresh token válido.
     *
     * @param request requisição com o refresh token
     * @return novos access e refresh tokens
     */
    @Transactional
    public RefreshTokenResponse execute(RefreshTokenRequest request) {
        log.debug("[RefreshToken] Solicitação de renovação de token");
        return refreshTokenService.refreshAccessToken(request.getRefreshToken());
    }
}

