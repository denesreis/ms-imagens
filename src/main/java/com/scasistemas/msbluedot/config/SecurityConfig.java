package com.scasistemas.msbluedot.config;

import com.scasistemas.msbluedot.security.CustomUserDetailsService;
import com.scasistemas.msbluedot.security.JwtAuthenticationFilter;
import com.scasistemas.msbluedot.security.SyncApiKeyFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Configuração central de segurança da aplicação.
 *
 * <p>
 * Endpoints públicos:
 * <ul>
 * <li>POST {@code /api/v1/auth/**} — login e refresh</li>
 * <li>GET {@code /api/v1/imagens/ean/**} — consulta pública por EAN (tipo
 * ABERTO)</li>
 * <li>GET {@code /actuator/health} — health check</li>
 * <li>GET {@code /v3/api-docs/**}, {@code /swagger-ui/**} — documentação</li>
 * </ul>
 * </p>
 *
 * <p>
 * Autorização por role está configurada em {@code @EnableMethodSecurity}
 * usando {@code @IsAdministrador} e {@code @IsUsuario} nas camadas de
 * serviço e controller.
 * </p>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

        private final JwtAuthenticationFilter jwtAuthenticationFilter;
        private final SyncApiKeyFilter syncApiKeyFilter;
        private final CustomUserDetailsService userDetailsService;

        public SecurityConfig(
                        @Qualifier("newJwtAuthenticationFilter") JwtAuthenticationFilter jwtAuthenticationFilter,
                        @Qualifier("newSyncApiKeyFilter") SyncApiKeyFilter syncApiKeyFilter,
                        @Qualifier("newCustomUserDetailsService") CustomUserDetailsService userDetailsService) {
                this.jwtAuthenticationFilter = jwtAuthenticationFilter;
                this.syncApiKeyFilter = syncApiKeyFilter;
                this.userDetailsService = userDetailsService;
        }

        /**
         * BCrypt com 12 rounds — recomendado para 2026.
         */
        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder(12);
        }

        /**
         * Provider de autenticação DAO: valida usuário + senha BCrypt.
         */
        @Bean
        public AuthenticationProvider authenticationProvider() {
                DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
                provider.setUserDetailsService(userDetailsService);
                provider.setPasswordEncoder(passwordEncoder());
                return provider;
        }

        /**
         * {@link AuthenticationManager} exposto como bean para injeção nos Use Cases.
         */
        @Bean
        public AuthenticationManager authenticationManager(
                        AuthenticationConfiguration config) throws Exception {
                return config.getAuthenticationManager();
        }

        /**
         * Configura a cadeia de filtros de segurança HTTP.
         */
        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                // Desabilitar CSRF — API stateless com JWT
                                .csrf(AbstractHttpConfigurer::disable)

                                // CORS
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                                // Session stateless
                                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                                // Autorizações
                                .authorizeHttpRequests(auth -> auth
                                                // Endpoints públicos de autenticação
                                                .requestMatchers(HttpMethod.POST,
                                                                "/api/v1/auth/login",
                                                                "/api/v1/auth/refresh",
                                                                "/api/v2/auth/login",
                                                                "/api/v2/auth/refresh")
                                                .permitAll()

                                                // Endpoints internos de sincronização (protegidos por X-Sync-Key no filtro)
                                                .requestMatchers(HttpMethod.POST,
                                                                "/api/v1/sync/**",
                                                                "/api/v2/sync/**")
                                                .permitAll()

                                                // Consulta pública de imagens por EAN
                                                .requestMatchers(HttpMethod.GET,
                                                                "/api/v1/imagens/ean/**",
                                                                "/api/v2/imagens/ean/**")
                                                .permitAll()

                                                // Actuator health e health customizado
                                                .requestMatchers(HttpMethod.GET,
                                                                "/actuator/health",
                                                                "/api/v1/health",
                                                                "/api/v2/health")
                                                .permitAll()

                                                // Swagger / OpenAPI
                                                .requestMatchers(
                                                                "/v3/api-docs/**",
                                                                "/api-docs/**",
                                                                "/swagger-ui/**",
                                                                "/swagger-ui.html")
                                                .permitAll()

                                                // Tudo mais exige autenticação
                                                .anyRequest().authenticated())

                                // 401 para não autenticado, 403 para não autorizado
                                .exceptionHandling(ex -> ex
                                                .authenticationEntryPoint(
                                                                (req, res, authEx) -> res.sendError(
                                                                                HttpServletResponse.SC_UNAUTHORIZED,
                                                                                "Unauthorized"))
                                                .accessDeniedHandler(
                                                                (req, res, accEx) -> res.sendError(
                                                                                HttpServletResponse.SC_FORBIDDEN,
                                                                                "Forbidden")))

                                // Provedor de autenticação
                                .authenticationProvider(authenticationProvider())

                                // JWT Filter antes do filtro padrão de username/password
                                .addFilterBefore(jwtAuthenticationFilter,
                                                UsernamePasswordAuthenticationFilter.class)

                                // SyncApiKeyFilter valida X-Sync-Key antes do JWT filter
                                .addFilterBefore(syncApiKeyFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

        /**
         * Configuração CORS permissiva para ambiente de desenvolvimento.
         *
         * <p>
         * <strong>Atenção:</strong> em produção, substituir {@code allowedOrigins("*")}
         * pela lista real de origens permitidas.
         * </p>
         */
        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration config = new CorsConfiguration();
                config.setAllowedOriginPatterns(List.of("*"));
                config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
                config.setAllowedHeaders(List.of("*"));
                config.setAllowCredentials(true);
                config.setMaxAge(3600L);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", config);
                return source;
        }
}
