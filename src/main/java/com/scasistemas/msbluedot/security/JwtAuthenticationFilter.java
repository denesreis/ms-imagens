package com.scasistemas.msbluedot.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
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

@Slf4j
@Component("newJwtAuthenticationFilter")
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklistService tokenBlacklistService;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(
            @Qualifier("newJwtTokenProvider") JwtTokenProvider jwtTokenProvider,
            @Qualifier("newTokenBlacklistService") TokenBlacklistService tokenBlacklistService,
            @Qualifier("newCustomUserDetailsService") CustomUserDetailsService userDetailsService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.tokenBlacklistService = tokenBlacklistService;
        this.userDetailsService = userDetailsService;
    }

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

    private boolean authenticateToken(String token, HttpServletRequest request) {
        try {
            if (!jwtTokenProvider.validateToken(token)) return false;
            if (tokenBlacklistService.isBlacklisted(token)) return false;
            if (!jwtTokenProvider.isAccessToken(token)) return false;
            if (SecurityContextHolder.getContext().getAuthentication() != null) return false;

            String username = jwtTokenProvider.getUsernameFromToken(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

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
