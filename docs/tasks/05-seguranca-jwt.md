# Task 05 - Segurança e Autenticação JWT

## Objetivo
Implementar autenticação e autorização com JWT, incluindo refresh token, logout, proteção contra brute force e audit log

## Subtasks

### 5.1 - Criar DTOs de Autenticação
- [x] `LoginRequest`
  - nome (username)
  - senha (password)
  
- [x] `LoginResponse`
  - accessToken, refreshToken, tipo (Bearer)
  - expiresIn (30 min), refreshExpiresIn (7 dias)
  - usuario (id, nome, role, idEmpresa)

- [x] `RefreshTokenRequest`
  - refreshToken

- [x] `RefreshTokenResponse`
  - accessToken, refreshToken (rotacionado), tipo, expiresIn

### 5.2 - Implementar JWT Service

#### 5.2.1 - JwtTokenProvider
- [x] Criar `JwtTokenProvider` service
- [x] Método `generateAccessToken(Usuario usuario): String`
  - Claims: idEmpresa, role, nome (sub), tokenType=ACCESS
  - Expiração: 30 minutos
  - Assinar com secret (mínimo 256 bits para HS256)
  
- [x] Método `generateRefreshToken(Usuario usuario): String`
  - Claims: idEmpresa, nome, tokenType=REFRESH
  - Expiração: 7 dias

- [x] Método `validateToken(String token): boolean`
  - Verificar assinatura e expiração
  - **Blacklist verificada no filtro (não no provider)**

- [x] Método `getUsernameFromToken(String token): String`
- [x] Método `getIdEmpresaFromToken(String token): Long`
- [x] Método `getRoleFromToken(String token): RoleEnum`
- [x] Método `getExpirationDateFromToken(String token): Date`
- [x] Método `getTokenTypeFromToken(String token): String`
- [x] Métodos `isAccessToken()` / `isRefreshToken()`

### 5.3 - Implementar Refresh Token Service

#### 5.3.1 - RefreshTokenService
- [x] Criar `RefreshTokenService`
- [x] Método `createRefreshToken(Usuario usuario): String`
  - Gera refresh token JWT
  - Persiste hash SHA-256 no banco (tb_refresh_token)
  - Retorna token plain text
  
- [x] Método `refreshAccessToken(String refreshToken): RefreshTokenResponse`
  - Valida token (hash SHA-256 → lookup no banco)
  - Verifica não revogado e não expirado
  - **Rotaciona refresh token** (revoga antigo, cria novo)
  - Retorna novos access + refresh tokens

- [x] Método `revokeRefreshToken(String refreshToken): void`
  - Marca como revogado no banco

- [x] Método `revokeAllUserTokens(Long userId): void`
  - Revoga todos os refresh tokens do usuário

### 5.4 - Implementar Token Blacklist (Logout JWT)

#### 5.4.1 - TokenBlacklistService
- [x] Criar `TokenBlacklistService`
- [x] Usar **Caffeine Cache** (`blacklistCaffeineCache`, TTL 31 min)
  - TTL ligeiramente maior que access token (30 min)
  - Tokens expirados removidos automaticamente
  
- [x] Método `blacklistToken(String token): void`
  - Extrai expiração do token
  - Adiciona ao cache se ainda não expirado
  
- [x] Método `isBlacklisted(String token): boolean`
  - Verifica presença no cache Caffeine

### 5.5 - Implementar Proteção Brute Force

#### 5.5.1 - LoginAttemptService
- [x] Criar `LoginAttemptService`
- [x] Usar **Caffeine Cache** (`loginAttemptsCaffeineCache`, TTL 15 min)

- [x] Método `loginFailed(String username): void`
  - Incrementa contador no cache
  - Log de aviso quando atingir o limite

- [x] Método `loginSucceeded(String username): void`
  - Invalida a entry do cache (reset pelo login bem-sucedido)

- [x] Método `isBlocked(String username): boolean`
  - Verifica se tentativas >= maxAttempts

- [x] Método `getAttempts(String username): int`

- [x] Configurações via `SecurityProperties`:
  - `app.security.login.max-attempts: 5`
  - `app.security.login.block-duration: 900000` (15 min)

### 5.6 - Implementar UserDetailsService
- [x] Criar `CustomUserDetailsService implements UserDetailsService`
  - `loadUserByUsername(String nome)`: busca do `IUsuarioRepository`
- [x] Criar `UserPrincipal implements UserDetails`
  - Authorities: `ROLE_ADMINISTRADOR` / `ROLE_USUARIO`
  - `isAccountNonLocked()` baseado em `usuario.estaBloqueado()`
  - `isEnabled()` baseado em `ativo`

### 5.7 - Filtros de Segurança

#### 5.7.1 - JwtAuthenticationFilter
- [x] Criar `JwtAuthenticationFilter extends OncePerRequestFilter`
- [x] Extrai token do header `Authorization: Bearer <token>`
- [x] Valida assinatura/expiração via `JwtTokenProvider`
- [x] **Verifica blacklist** via `TokenBlacklistService`
- [x] Confirma que é ACCESS token (rejeita REFRESH tokens)
- [x] Popula `SecurityContextHolder`
- [x] Trata exceções de token inválido/expirado com log e context clear

### 5.8 - Configuração de Segurança

#### 5.8.1 - SecurityConfig
- [x] `@EnableWebSecurity` + `@EnableMethodSecurity`
- [x] Endpoints públicos: POST auth/login, POST auth/refresh, GET imagens/ean/**, health, swagger
- [x] Endpoints protegidos: todos os demais
- [x] CORS configurado (`allowedOriginPatterns(*)`)
- [x] CSRF desabilitado (stateless)
- [x] Session Management STATELESS
- [x] `JwtAuthenticationFilter` adicionado antes de `UsernamePasswordAuthenticationFilter`

#### 5.8.2 - Password Encoder
- [x] `BCryptPasswordEncoder(12)` — 12 rounds (2026)
- [x] `AuthenticationManager` exposto como bean
- [x] `DaoAuthenticationProvider` configurado

#### 5.8.3 - CacheConfig
- [x] `CacheConfig` com beans Caffeine para blacklist (31 min) e login-attempts (15 min)

### 5.9 - Authorization por Role
- [x] `@EnableMethodSecurity` ativo no `SecurityConfig`
- [x] Annotations customizadas:
  - `@IsAdministrador` → `@PreAuthorize("hasRole('ADMINISTRADOR')")`
  - `@IsUsuario` → `@PreAuthorize("hasAnyRole('USUARIO','ADMINISTRADOR')")`

### 5.10 - Use Cases de Autenticação

- [x] `AuthenticateUserUseCase`
  - Verifica bloqueio em cache + banco
  - Autentica via `AuthenticationManager` (BCrypt)
  - Registra falha e bloqueia conta via `registrarFalhaLogin(max, duration)`
  - Reseta contador após sucesso
  - Gera access + refresh tokens
  - Grava audit log em todos os casos

- [x] `RefreshTokenUseCase`
  - Delega para `RefreshTokenService.refreshAccessToken()`
  - Rotação de refresh token automática

- [x] `LogoutUseCase`
  - Blacklist do access token
  - Revogação do refresh token no banco
  - Limpa `SecurityContextHolder`
  - Grava audit log

- [x] `AuthController` (`/api/v1/auth`)
  - POST /login — público
  - POST /refresh — público
  - POST /logout — autenticado (204 No Content)
  - Extrai IP do header `X-Forwarded-For`

### 5.11 - Utilitários de Segurança
- [x] Criar `SecurityUtils` (`@UtilityClass`)
  - `getCurrentPrincipal(): UserPrincipal`
  - `getCurrentUser(): Usuario`
  - `getCurrentIdEmpresa(): Long`
  - `getCurrentUsername(): String`
  - `hasRole(RoleEnum role): boolean`
  - `isAdministrador(): boolean`
  - `getCurrentToken(): String`

## Critérios de Aceite
- [x] Login implementado retornando access token + refresh token
- [x] Access token válido por 30 minutos (configurável)
- [x] Refresh token válido por 7 dias com rotação
- [x] Logout invalidando tokens (blacklist Caffeine + revogação BD)
- [x] Proteção brute force: bloqueio após 5 tentativas em 15 min
- [x] Endpoints protegidos exigindo autenticação (`SecurityConfig`)
- [x] Claims do token contendo idEmpresa
- [x] Autorização por role (`@IsAdministrador`, `@IsUsuario`)
- [x] Senha criptografada com BCrypt (strength 12)
- [x] **POST /api/usuarios restrito a ADMINISTRADOR** (via `@IsAdministrador`)
- [x] Audit log registrando login/logout/falhas
- [x] BUILD SUCCESS confirmado
- [ ] Testes de autenticação end-to-end (Task 08)
