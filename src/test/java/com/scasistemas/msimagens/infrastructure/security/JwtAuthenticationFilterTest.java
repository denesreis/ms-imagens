package com.scasistemas.msimagens.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenticationFilter - Testes unitários")
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private TokenBlacklistService tokenBlacklistService;
    @Mock
    private CustomUserDetailsService userDetailsService;
    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Deve autenticar usuário com token válido")
    void deveAutenticarComTokenValido() throws ServletException, IOException {
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer valid-token");

        com.scasistemas.msimagens.domain.entities.Usuario usuario = com.scasistemas.msimagens.domain.entities.Usuario
                .builder()
                .id(1L).nome("admin")
                .role(com.scasistemas.msimagens.domain.enums.RoleEnum.ADMINISTRADOR)
                .idEmpresa(1L).ativo(true).tentativasLogin(0).build();
        UserPrincipal principal = new UserPrincipal(usuario);

        when(jwtTokenProvider.validateToken("valid-token")).thenReturn(true);
        when(tokenBlacklistService.isBlacklisted("valid-token")).thenReturn(false);
        when(jwtTokenProvider.isAccessToken("valid-token")).thenReturn(true);
        when(jwtTokenProvider.getUsernameFromToken("valid-token")).thenReturn("admin");
        when(userDetailsService.loadUserByUsername("admin")).thenReturn(principal);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("admin");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Deve prosseguir sem autenticação quando não há Authorization header")
    void deveProsseguirSemAuthorizationHeader() throws ServletException, IOException {
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Deve rejeitar token inválido")
    void deveRejeitarTokenInvalido() throws ServletException, IOException {
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer invalid-token");

        when(jwtTokenProvider.validateToken("invalid-token")).thenReturn(false);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Deve rejeitar token na blacklist (logout)")
    void deveRejeitarTokenNaBlacklist() throws ServletException, IOException {
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer blacklisted-token");

        when(jwtTokenProvider.validateToken("blacklisted-token")).thenReturn(true);
        when(tokenBlacklistService.isBlacklisted("blacklisted-token")).thenReturn(true);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Deve rejeitar token do tipo REFRESH")
    void deveRejeitarTokenRefresh() throws ServletException, IOException {
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer refresh-token");

        when(jwtTokenProvider.validateToken("refresh-token")).thenReturn(true);
        when(tokenBlacklistService.isBlacklisted("refresh-token")).thenReturn(false);
        when(jwtTokenProvider.isAccessToken("refresh-token")).thenReturn(false);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Deve ignorar header sem prefixo Bearer")
    void deveIgnorarHeaderSemBearer() throws ServletException, IOException {
        request.addHeader(HttpHeaders.AUTHORIZATION, "Basic dXNlcjpwYXNz");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }
}
