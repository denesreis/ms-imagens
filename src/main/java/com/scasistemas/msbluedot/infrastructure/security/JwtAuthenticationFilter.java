package com.scasistemas.msbluedot.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro JWT que intercepta toda requisição, extrai o Bearer token do header
 * {@code Authorization}, valida e popula o {@link SecurityContextHolder}.
 *
 * <p>
 * Fluxo:
 * <ol>
 * <li>Extrai o token do header</li>
 * <li>Valida assinatura e expiração ({@link JwtTokenProvider})</li>
 * <li>Verifica blacklist ({@link TokenBlacklistService})</li>
 * <li>Confirma que é um ACCESS token (não REFRESH)</li>
 * <li>Carrega os dados do usuário ({@link CustomUserDetailsService})</li>
 * <li>Seta a autenticação no contexto</li>
 * </ol>
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklistService tokenBlacklistService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String token = extractToken(request);

        if (token != null && authenticateToken(token, request)) {
            log.debug("[JwtFilter] Autenticação definida para: {}",
                    SecurityContextHolder.getContext().getAuthentication().getName());
        }

        filterChain.doFilter(request, response);
    }

    // ─────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────

    private boolean authenticateToken(String token, HttpServletRequest request) {
        try {
            // 1. Valida assinatura e expiração
            if (!jwtTokenProvider.validateToken(token)) {
                log.debug("[JwtFilter] Token inválido ou expirado");
                return false;
            }

            // 2. Verifica blacklist (logout)
            if (tokenBlacklistService.isBlacklisted(token)) {
                log.debug("[JwtFilter] Token na blacklist (logout)");
                return false;
            }

            // 3. Só aceita ACCESS tokens neste filtro
            if (!jwtTokenProvider.isAccessToken(token)) {
                log.debug("[JwtFilter] Token não é do tipo ACCESS");
                return false;
            }

            // 4. Sem autenticação já existente no contexto
            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                return false;
            }

            // 5. Carregar usuário
            String username = jwtTokenProvider.getUsernameFromToken(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // 6. Setar autenticação no contexto
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(auth);

            return true;

        } catch (Exception e) {
            log.warn("[JwtFilter] Erro ao processar token: {}", e.getMessage());
            SecurityContextHolder.clearContext();
            return false;
        }
    }

    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}

