package com.scasistemas.msimagens.application.usecases;

import com.scasistemas.msimagens.application.dto.auth.RefreshTokenRequest;
import com.scasistemas.msimagens.application.dto.auth.RefreshTokenResponse;
import com.scasistemas.msimagens.application.services.RefreshTokenService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RefreshTokenUseCase - Testes unitários")
class RefreshTokenUseCaseTest {

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private RefreshTokenUseCase refreshTokenUseCase;

    @Test
    @DisplayName("Deve delegar renovação de token ao RefreshTokenService")
    void deveDelegarRenovacaoDeToken() {
        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .refreshToken("old-refresh-token")
                .build();

        RefreshTokenResponse expected = RefreshTokenResponse.builder()
                .accessToken("new-access-token")
                .refreshToken("new-refresh-token")
                .expiresIn(1800L)
                .build();

        when(refreshTokenService.refreshAccessToken("old-refresh-token")).thenReturn(expected);

        RefreshTokenResponse result = refreshTokenUseCase.execute(request);

        assertThat(result).isNotNull();
        assertThat(result.getAccessToken()).isEqualTo("new-access-token");
        assertThat(result.getRefreshToken()).isEqualTo("new-refresh-token");
        verify(refreshTokenService).refreshAccessToken("old-refresh-token");
    }
}
