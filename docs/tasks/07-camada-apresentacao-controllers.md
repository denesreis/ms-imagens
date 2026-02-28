# Task 07 - Camada de Apresentação - Controllers

## Objetivo
Implementar os controllers REST com todos os endpoints necessários

## Subtasks

### 7.1 - Controller de Autenticação
- [x] `AuthController` - **`/api/v1/auth`**
  - POST `/api/v1/auth/login`
    - Body: LoginRequest
    - Response: LoginResponse (accessToken + refreshToken)
    - Status: 200 OK ou 401 Unauthorized
    - **Registra audit log (LOGIN ou LOGIN_FALHA)**
    - **Proteção contra brute force** (retorna 423 Locked se conta bloqueada)

  - POST `/api/v1/auth/refresh` **(público)**
    - Body: `{ "refreshToken": "..." }`
    - Response: LoginResponse (novo accessToken + novo refreshToken)
    - Status: 200 OK ou 401 Unauthorized (token inválido/expirado/revogado)
    - Implementar **token rotation** (novo refresh token a cada uso)

  - POST `/api/v1/auth/logout` **(autenticado)**
    - Header: Authorization Bearer {accessToken}
    - Body: `{ "refreshToken": "..." }` (opcional)
    - Status: 200 OK
    - Adiciona access token ao **blacklist (Caffeine cache)**
    - Revoga **todos os refresh tokens** do usuário no banco
    - **Registra audit log (LOGOUT)**

### 7.2 - Controller de Empresa

- [x] `EmpresaController` - **`/api/v1/empresas`**
  - POST `/api/v1/empresas` (criar) - @IsAdministrador
    - Body: EmpresaRequest
    - Response: EmpresaResponse
    - Status: 201 Created
    
  - GET `/api/v1/empresas/{id}` (buscar por ID)
    - Response: EmpresaResponse
    - Status: 200 OK ou 404 Not Found
    
  - GET `/api/v1/empresas` (listar todas) - @IsAdministrador
    - **Query params: page, size, sort** (paginação padronizada)
    - Response: **Page<EmpresaResponse>**
    - Status: 200 OK
    
  - PUT `/api/v1/empresas/{id}` (atualizar) - @IsAdministrador
    - Body: EmpresaRequest
    - Response: EmpresaResponse
    - Status: 200 OK ou 404 Not Found
    
  - DELETE `/api/v1/empresas/{id}` (deletar) - @IsAdministrador
    - Status: 204 No Content ou 404 Not Found

### 7.3 - Controller de Usuario

- [x] `UsuarioController` - **`/api/v1/usuarios`**
  - POST `/api/v1/usuarios` (criar) - **@IsAdministrador** (somente ADMIN pode criar usuários)
    - Body: UsuarioRequest
    - Response: UsuarioResponse
    - Status: 201 Created
    
  - GET `/api/v1/usuarios/{id}` (buscar por ID)
    - Verificar permissões
    - Response: UsuarioResponse
    - Status: 200 OK ou 404 Not Found
    
  - GET `/api/v1/usuarios` (listar)
    - Filtrar por empresa se não for ADMIN
    - **Query params: page, size, sort** (paginação padronizada)
    - Response: **Page<UsuarioResponse>**
    - Status: 200 OK
    
  - PUT `/api/v1/usuarios/{id}` (atualizar)
    - Verificar permissões
    - Body: UsuarioRequest
    - Response: UsuarioResponse
    - Status: 200 OK ou 404 Not Found
    
  - DELETE `/api/v1/usuarios/{id}` (deletar)
    - Verificar permissões
    - Status: 204 No Content ou 404 Not Found

### 7.4 - Controller de Produto

- [x] `ProdutoController` - **`/api/v1/produtos`**
  - POST `/api/v1/produtos` (criar com imagens)
    - Content-Type: multipart/form-data
    - Parts:
      - produto (JSON - ProdutoRequest)
      - imagens (MultipartFile[])
      - tiposArmazenamento (int[])
    - Response: ProdutoResponse (com URLs das imagens)
    - Status: 201 Created
    
  - GET `/api/v1/produtos/{id}` (buscar por ID)
    - Response: ProdutoResponse (com imagens)
    - Status: 200 OK ou 404 Not Found
    
  - GET `/api/v1/produtos` (listar)
    - Query params: page, size, sort
    - Filtrar por empresa se não for ADMIN
    - Response: Page<ProdutoResponse>
    - Status: 200 OK
    
  - PUT `/api/v1/produtos/{id}` (atualizar com novas imagens)
    - Content-Type: multipart/form-data
    - Parts: produto (JSON), imagens (opcional)
    - Response: ProdutoResponse
    - Status: 200 OK ou 404 Not Found
    
  - DELETE `/api/v1/produtos/{id}` (deletar)
    - Verificar permissões
    - Status: 204 No Content ou 404 Not Found

### 7.5 - Controller de Imagem

- [x] `ImagemController` - **`/api/v1/imagens`**
  - POST `/api/v1/imagens` (criar/upload)
    - Content-Type: multipart/form-data
    - Parts:
      - imagem (JSON - ImagemRequest sem arquivo)
      - file (MultipartFile)
    - Response: ImagemResponse (com URL)
    - Status: 201 Created
    
  - GET `/api/v1/imagens/{id}` (buscar por ID)
    - Verificar permissões (aberta ou mesma empresa)
    - Response: ImagemResponse
    - Status: 200 OK ou 404 Not Found
    
  - GET `/api/v1/imagens/produto/{idProduto}` (buscar por produto)
    - **Query params: page, size** (paginação padronizada)
    - Response: **Page<ImagemResponse>**
    - Status: 200 OK
    
  - GET `/api/v1/imagens/ean/{codigoEan}` (buscar por EAN - **PÚBLICO, sem JWT**)
    - **Endpoint público** - configurado no SecurityConfig (sem autenticação)
    - Retorna apenas imagens com tipoArmazenamento = ABERTO **e status = ATIVO**
    - Response: List<ImagemProdutoResponse>
    - Status: 200 OK
    - Facilita integração com e-commerces e catálogos externos
    
  - PUT `/api/v1/imagens/{id}` (atualizar)
    - Atualizar metadados ou tipo
    - Body: ImagemRequest
    - Response: ImagemResponse
    - Status: 200 OK ou 404 Not Found
    
  - DELETE `/api/v1/imagens/{id}` (deletar)
    - Deleta da Cloudflare e do banco
    - Status: 204 No Content ou 404 Not Found

### 7.6 - Controller de Health Check
- [x] `HealthController` - **`/api/v1/health`**
  - GET `/api/v1/health` (público)
    - Verificar conexão com banco
    - Verificar token Cloudflare
    - Response: HealthResponse
    - Status: 200 OK

### 7.7 - Exception Handler Global

- [x] `GlobalExceptionHandler` com `@RestControllerAdvice`
  - `@ExceptionHandler(ResourceNotFoundException.class)` → 404
  - `@ExceptionHandler(UnauthorizedException.class)` → 401
  - `@ExceptionHandler(BusinessException.class)` → 400
  - `@ExceptionHandler(CloudflareException.class)` → 502
  - `@ExceptionHandler(AccountLockedException.class)` → **423 Locked** (conta bloqueada por brute force)
  - `@ExceptionHandler(TokenRevokedException.class)` → **401 Unauthorized** (token revogado)
  - **`@ExceptionHandler(DuplicateUsernameException.class)` → 409 Conflict** (username já existe)
  - `@ExceptionHandler(MethodArgumentNotValidException.class)` → 400
  - `@ExceptionHandler(Exception.class)` → 500
  
- [x] Criar `ErrorResponse` DTO padronizado
  - timestamp
  - status
  - error
  - message
  - path

### 7.8 - Validações nos DTOs
- [x] Adicionar anotações de validação nos Request DTOs:
  - `@NotNull`, `@NotBlank`, `@Size`
  - `@Email`, `@Pattern`
  - Validações customizadas se necessário

## Critérios de Aceite
- [x] Todos os endpoints implementados
- [x] Documentação OpenAPI gerada automaticamente
- [x] Validações funcionando
- [x] Exception handling padronizado
- [x] Respostas HTTP corretas (status codes)
- [x] Content negotiation funcionando
- [x] Multipart file upload funcionando
- [x] **Endpoints /auth/refresh e /auth/logout funcionando**
- [x] **Proteção brute force retornando 423 Locked**
- [x] **Audit log integrado nas ações de autenticação**
- [x] **Versionamento /api/v1/ em todos os endpoints**
- **Paginação padronizada (Page<T>) em todos os endpoints de listagem**
- **Endpoint /imagens/ean/{codigoEan} público (sem autenticação)**
- **DuplicateUsernameException → 409 Conflict**
- **Soft delete transparente (Hibernate @SQLDelete/@SQLRestriction)**
