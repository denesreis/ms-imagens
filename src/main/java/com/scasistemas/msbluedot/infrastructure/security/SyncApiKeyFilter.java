package com.scasistemas.msbluedot.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro de API Key para o endpoint de sincronização interna (/api/v1/sync/**).
 *
 * <p>
 * Verifica o header {@code X-Sync-Key}. Requisições sem a chave correta
 * recebem 403 Forbidden antes mesmo de chegar ao controller.
 * </p>
 *
 * <p>
 * Este filtro só atua sobre paths que começam com {@code /api/v1/sync/}.
 * Outras rotas são ignoradas.
 * </p>
 */
@Slf4j
@Component
public class SyncApiKeyFilter extends OncePerRequestFilter {

    private static final String SYNC_PATH_PREFIX = "/api/v1/sync/";
    private static final String HEADER_NAME = "X-Sync-Key";

    @Value("${app.sync.api-key}")
    private String expectedApiKey;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        // Só intercepta rotas de sincronização
        if (!path.startsWith(SYNC_PATH_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String providedKey = request.getHeader(HEADER_NAME);

        if (providedKey == null || !providedKey.equals(expectedApiKey)) {
            log.warn("[SyncApiKeyFilter] Chave inválida ou ausente para path={} ip={}",
                    path, request.getRemoteAddr());
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"API key inválida ou ausente\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
