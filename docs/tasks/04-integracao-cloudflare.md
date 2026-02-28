# Task 04 - Integração com Cloudflare Images API

## Objetivo
Implementar cliente para integração com Cloudflare Images API

## Subtasks

### 4.1 - Criar DTOs da Cloudflare

#### 4.1.1 - Request/Response DTOs
- [x] `CloudflareTokenVerifyResponse`
  - result (id, status)
  - success
  - errors
  - messages
  
- [x] `CloudflareUploadImageResponse`
  - result (id, filename, uploaded, requireSignedURLs, variants[])
  - success
  - errors
  - messages
  
- [x] `CloudflareErrorResponse`
  - errors[]
  - messages[]

### 4.2 - Criar Interface de Serviço
- [x] `ICloudflareImageService` (domain service interface)
  - `verifyToken(String token): boolean`
  - `uploadImage(byte[] imageBytes, String filename, String contentType): CloudflareUploadResult`
  - `deleteImage(String imageId): boolean`
- [x] `CloudflareUploadResult` (value object de domínio)
  - status ATIVO ou ERRO, imageId, url, errorMessage
  - factory methods `success()` e `error()`

### 4.3 - Implementar Cliente Cloudflare

#### 4.3.1 - CloudflareImageClient
- [x] Criar `CloudflareImageClient` usando WebClient
- [x] Configurar base URL: `https://api.cloudflare.com/client/v4`
- [x] Implementar método `verifyToken()`
  - GET `/user/tokens/verify`
  - Header: Authorization com token
  - Retry automático (3x, backoff 1s→10s), sem retry em 4xx/5xx
  
- [x] Implementar método `uploadImage()`
  - POST `/accounts/{accountId}/images/v1`
  - Header: Authorization com postToken
  - Body: multipart/form-data (byte[] + filename + contentType)
  - Extrai URL variante "public" da lista de variants[]
  - **Retorna ATIVO se sucesso, ERRO se falha** (sem retry para evitar duplicatas)
  
- [x] Implementar método `deleteImage()`
  - DELETE `/accounts/{accountId}/images/v1/{imageId}`
  - Header: Authorization com postToken
  - Retry automático (3x, backoff 1s→10s)

### 4.4 - Tratamento de Erros
- [x] Criar exception handler para erros da API (`CloudflareException` existente no domínio)
- [x] Implementar retry policy (Reactor `Retry.backoff(3, 1s).maxBackoff(10s)`)
- [x] Implementar timeout configuration (connect + read via `CloudflareProperties.Timeout`)
- [x] Log de requisições e respostas (filtros DEBUG no `ExchangeFilterFunction`)
- [x] **Em caso de falha, retorna `CloudflareUploadResult.error()` com status ERRO** (sem throw)

### 4.5 - Configuração
- [x] Criar `CloudflareConfig`
  - Bean `cloudflareWebClient` com Reactor Netty
  - Timeouts de conexão e leitura via `CloudflareProperties.Timeout`
  - Buffer 16 MB para upload (`maxInMemorySize`)
  - Pool de conexões (50 max, 100 pendentes)
  - Filtros de log request/response (nível DEBUG)
  
- [x] `CloudflareProperties` (já existia na Task 01)
  - accountId, getToken, postToken, baseUrl, maxFileSize
  - Inner class `Timeout` com connect + read ms

### 4.6 - Validações
- [x] Validar tipos de arquivo aceitos (PNG, JPG, JPEG, GIF, WebP) via `isSupported()` no `ImageCompressionService`
- [ ] Validar tamanho máximo de arquivo (a implementar no Use Case / Controller)
- [ ] Validar token antes de upload (a implementar no Use Case)

### 4.7 - Compressão de Imagem (Pré-upload)
- [x] Implementar `ImageCompressionService`
  - Usa **Thumbnailator** (net.coobird:thumbnailator 0.4.20)
  - Configurável via `app.image.compression.*` (já em `ImageCompressionProperties`)
  - Só comprime se arquivo > 1 MB (`MIN_SIZE_TO_COMPRESS_BYTES = 1_048_576`)
  - Suporte a: jpeg, jpg, png, gif, webp
  - WebP → JPEG (fallback; ImageIO nativo não suporta escrita WebP)
  - `keepAspectRatio(true)` para não distorcer imagens
  - Fail-safe: em caso de erro na compressão, retorna bytes originais
  - Log da redução: `'filename' comprimido: X.XX MB → Y.YY MB (redução de Z.Z%)`

## Critérios de Aceite
- [x] Cliente Cloudflare implementado (`CloudflareImageClient`)
- [x] Verificação de token implementada (`verifyToken`)
- [x] Upload implementado com multipart/form-data (`uploadImage`)
- [x] Tratamento de erros com `CloudflareException` + fail-safe
- [x] Configurações externalizadas em `CloudflareProperties`
- [x] Logs adequados (DEBUG para requisições, INFO para resultados)
- [x] **Compressão de imagem funcional (Thumbnailator)**
- [x] **Redução de tamanho mensurável nos uploads**
- [x] BUILD SUCCESS confirmado
- [ ] Upload de imagens testado com credenciais reais (Task 08)
- [ ] Validação de token end-to-end (Task 08)
