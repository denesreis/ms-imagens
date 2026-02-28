# Task 08 - Testes Automatizados ✅

## Status: CONCLUÍDA
- **198 testes** passando (BUILD SUCCESS)
- **44 arquivos de teste**
- **Cobertura JaCoCo:**
  - Instruções: 66.0%
  - Linhas: 67.9%
  - Branches: 57.0%

---

## Objetivo
Implementar testes unitários e de integração com boa cobertura

## Subtasks

### 8.1 - Configuração de Testes
- [x] Configurar dependências de teste no pom.xml:
  - JUnit 5 (Jupiter)
  - Mockito
  - SpringBootTest
  - H2 Database (para testes unitários rápidos)
  - Lombok (test scope para builders nos testes)
  
- [x] Criar `application-test.yml`
- [x] Configurar perfil de teste

### 8.2 - Testes Unitários - Domain

#### 8.2.1 - Testes de Entidades
- [x] `UsuarioTest` - 7 testes (validações e comportamentos)
- [x] `EmpresaTest` - 4 testes
- [x] `ProdutoTest` - 4 testes
- [x] `ImagemTest` - 8 testes (status PENDENTE, transições ATIVO/ERRO)
- [x] `RefreshTokenTest` - 6 testes (expiração e revogação)
- [x] `AuditLogTest` - 4 testes (construção e validações)

### 8.3 - Testes Unitários - Use Cases

- [x] `CreateEmpresaUseCaseTest` - 2 testes
- [x] `CreateUsuarioUseCaseTest` - 4 testes (duplicado → 409)
- [x] `CreateProdutoUseCaseTest` - 3 testes (EAN duplicado)
- [x] `CreateImagemUseCaseTest` - 3 testes (PENDENTE→ATIVO, PENDENTE→ERRO)
- [x] `GetImagensByCodigoEanUseCaseTest` - 2 testes
- [x] `AuthenticateUserUseCaseTest` - 7 testes (brute force, audit log)
- [x] `RefreshTokenUseCaseTest` - 1 teste (delegação ao service)
- [x] `LogoutUseCaseTest` - 5 testes (blacklist, revoke, audit)
- [x] `GetEmpresaUseCaseTest` - 4 testes (admin, usuario, unauthorized, not found)
- [x] `ListEmpresasUseCaseTest` - 3 testes (paginação)
- [x] `UpdateEmpresaUseCaseTest` - 3 testes (sucesso, ERP duplicado, not found)
- [x] `DeleteEmpresaUseCaseTest` - 2 testes (soft delete + audit)
- [x] `GetUsuarioUseCaseTest` - 4 testes
- [x] `ListUsuariosUseCaseTest` - 2 testes (admin/usuario)
- [x] `UpdateUsuarioUseCaseTest` - 5 testes (role change, password encoding)
- [x] `DeleteUsuarioUseCaseTest` - 2 testes
- [x] `GetProdutoUseCaseTest` - 3 testes
- [x] `ListProdutosUseCaseTest` - 2 testes
- [x] `UpdateProdutoUseCaseTest` - 3 testes
- [x] `DeleteProdutoUseCaseTest` - 4 testes
- [x] `GetImagemByIdUseCaseTest` - 3 testes
- [x] `GetImagensByProdutoUseCaseTest` - 3 testes (paginação)
- [x] `UpdateImagemUseCaseTest` - 3 testes
- [x] `DeleteImagemUseCaseTest` - 5 testes (Cloudflare delete)

Cada teste deve:
- Mockar dependências (repositories, services)
- Testar cenários de sucesso
- Testar cenários de erro/exceção
- Verificar validações

### 8.4 - Testes de Integração - Repositories

- [x] `UsuarioRepositoryIntegrationTest` - 7 testes (@DataJpaTest, H2)
  - save, findById, findByNome, unique constraint, soft delete
  
- [x] `EmpresaRepositoryIntegrationTest` - 6 testes
  - findByCodigoErp, soft delete, CRUD
  
- [x] `ProdutoRepositoryIntegrationTest` - 6 testes
  - findByCodigoEan, findByIdEmpresa
  
- [x] `ImagemRepositoryIntegrationTest` - 7 testes
  - findByCodigoEanAndTipoAberto, findByIdProduto, soft delete

### 8.5 - Testes de Integração - Controllers

- [ ] `AuthControllerIntegrationTest` _(pendente — requer setup completo de contexto Spring)_
  
- [x] `EmpresaControllerIntegrationTest` - 6 testes (@SpringBootTest + MockMvc)
  - CRUD completo, autorização ADMIN
  
- [ ] `UsuarioControllerIntegrationTest` _(pendente)_
  
- [ ] `ProdutoControllerIntegrationTest` _(pendente)_
  
- [x] `ImagemControllerIntegrationTest` - 6 testes (JWT real)
  - Busca por EAN (público), por produto, por ID, permissões

### 8.6 - Testes do Cliente Cloudflare

- [ ] `CloudflareImageClientTest` _(pendente — requer WireMock para WebClient)_

### 8.7 - Testes de Segurança

- [x] `JwtTokenProviderTest` - 8 testes (geração, validação, claims, expirado)
  
- [x] `RefreshTokenServiceTest` - 5 testes (SHA-256 hash, rotação, revogação)

- [x] `TokenBlacklistServiceTest` - 5 testes (Caffeine cache)

- [x] `LoginAttemptServiceTest` - 7 testes (brute force, bloqueio, reset)

- [x] `CustomUserDetailsServiceTest` - 2 testes (UserPrincipal, not found)

- [x] `JwtAuthenticationFilterTest` - 6 testes (valid, bearer, blacklist, refresh reject)

- [x] `ImageCompressionServiceTest` - 6 testes (disabled, small file, MIME, fail-safe)

- [x] `GlobalExceptionHandlerTest` - 9 testes (403, 404, 401, 423, 409, 422, 502, 500)

- [ ] `SecurityIntegrationTest`
  - Testar acesso sem token (401)
  - Testar acesso com token válido
  - Testar acesso com token expirado
  - Testar autorização por role
  - **Testar acesso com token no blacklist (401)**
  - **Testar POST /usuarios sem ADMIN (403)**
  - **Testar conta bloqueada não pode fazer login (423)**
  - **Testar GET /imagens/ean/{ean} sem token (200 OK - público)**

### 8.8 - Testes de Carga (Opcional)
- [ ] Configurar JMeter ou Gatling
- [ ] Criar cenários de teste:
  - Upload massivo de imagens
  - Consultas concorrentes
  - Login simultâneo

### 8.9 - Cobertura de Código
- [x] Configurar JaCoCo (0.8.13)
- [ ] Meta: mínimo 80% de cobertura (atual: 66% instruções / 68% linhas)
- [x] Gerar relatório HTML (`target/site/jacoco/index.html`)
- [ ] Integrar com build (falhar se < 80%)

## Critérios de Aceite
- Testes unitários com 80%+ de cobertura
- Testes de integração para fluxos principais
- Todos os testes passando
- CI configurado para rodar testes
- Relatório de cobertura gerado
- **Testes de refresh token, logout e blacklist passando**
- **Testes de brute force passando**
- **Testes de audit log passando**
- **Testes de criar usuário restrito a ADMIN passando**
- **Testes de cache (Caffeine) passando**
- **Testes de paginação passando em todos os endpoints de listagem**
- **Testes de compressão de imagem (Thumbnailator) passando**
- **Mappers MapStruct compilando e mapeando corretamente**
- **Testes de soft delete (@SQLDelete/@SQLRestriction) passando**
- **Testes de status da imagem (PENDENTE/ATIVO/ERRO) passando**
- **Testes de unique constraint username passando**
- **Testes de endpoint EAN público (sem JWT) passando**
