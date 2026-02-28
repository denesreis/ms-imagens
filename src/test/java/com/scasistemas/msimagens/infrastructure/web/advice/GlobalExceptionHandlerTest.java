package com.scasistemas.msimagens.infrastructure.web.advice;

import com.scasistemas.msimagens.application.dto.common.ErrorResponse;
import com.scasistemas.msimagens.domain.exceptions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GlobalExceptionHandler - Testes unitários")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/test");
    }

    @Test
    @DisplayName("Deve retornar 403 para AccessDeniedException")
    void deveRetornar403ParaAccessDenied() {
        ResponseEntity<ErrorResponse> response = handler.handleAccessDenied(
                new AccessDeniedException("Acesso negado"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(403);
    }

    @Test
    @DisplayName("Deve retornar 404 para ResourceNotFoundException")
    void deveRetornar404ParaResourceNotFound() {
        ResponseEntity<ErrorResponse> response = handler.handleResourceNotFound(
                new ResourceNotFoundException("Recurso não encontrado"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().status()).isEqualTo(404);
    }

    @Test
    @DisplayName("Deve retornar 401 para UnauthorizedException")
    void deveRetornar401ParaUnauthorized() {
        ResponseEntity<ErrorResponse> response = handler.handleUnauthorized(
                new UnauthorizedException("Não autorizado"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().status()).isEqualTo(401);
    }

    @Test
    @DisplayName("Deve retornar 401 para TokenRevokedException")
    void deveRetornar401ParaTokenRevoked() {
        ResponseEntity<ErrorResponse> response = handler.handleTokenRevoked(
                new TokenRevokedException("Token revogado"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Deve retornar 423 para AccountLockedException")
    void deveRetornar423ParaAccountLocked() {
        ResponseEntity<ErrorResponse> response = handler.handleAccountLocked(
                new AccountLockedException("Conta bloqueada", LocalDateTime.now().plusMinutes(15)), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.LOCKED);
        assertThat(response.getBody().status()).isEqualTo(423);
    }

    @Test
    @DisplayName("Deve retornar 409 para DuplicateUsernameException")
    void deveRetornar409ParaDuplicateUsername() {
        ResponseEntity<ErrorResponse> response = handler.handleDuplicateUsername(
                new DuplicateUsernameException("Nome duplicado"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("Deve retornar 422 para BusinessException")
    void deveRetornar422ParaBusiness() {
        ResponseEntity<ErrorResponse> response = handler.handleBusiness(
                new BusinessException("Erro de negócio"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    @DisplayName("Deve retornar 502 para CloudflareException")
    void deveRetornar502ParaCloudflare() {
        ResponseEntity<ErrorResponse> response = handler.handleCloudflare(
                new CloudflareException("Erro na Cloudflare"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
    }

    @Test
    @DisplayName("Deve retornar 500 para exceções genéricas")
    void deveRetornar500ParaExcecoesGenericas() {
        ResponseEntity<ErrorResponse> response = handler.handleGeneral(
                new RuntimeException("Erro inesperado"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().message()).contains("erro interno");
    }
}
