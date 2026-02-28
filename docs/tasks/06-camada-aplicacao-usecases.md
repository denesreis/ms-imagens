# Task 06 - Camada de Aplicação - Use Cases

## Objetivo
Implementar os casos de uso (regras de negócio) da aplicação

## Subtasks

### 6.1 - DTOs da Aplicação

#### 6.1.1 - DTOs de Empresa
- [x] `EmpresaRequest` (create/update)
- [x] `EmpresaResponse`

#### 6.1.2 - DTOs de Usuario
- [x] `UsuarioRequest` (create/update)
- [x] `UsuarioResponse` (sem senha)

#### 6.1.3 - DTOs de Produto
- [x] `ProdutoRequest`
- [x] `ProdutoResponse`
- [x] `ImagemUploadRequest` (arquivo + tipoArmazenamento)

#### 6.1.4 - DTOs de Imagem
- [x] `ImagemResponse`
- [x] `ImagemProdutoResponse` (para consultas por EAN/produto)

### 6.2 - Use Cases de Empresa

- [x] `CreateEmpresaUseCase`
  - Validar dados com Bean Validation
  - Verificar duplicidade codigoErp
  - Salvar empresa
  - Audit log (CRIAR_EMPRESA)
  - Restrito a ADMINISTRADOR

- [x] `UpdateEmpresaUseCase`
  - Validar existência
  - Verificar duplicidade codigoErp se alterado
  - Atualizar dados
  - Audit log (ATUALIZAR_EMPRESA)
  - Restrito a ADMINISTRADOR

- [x] `GetEmpresaUseCase`
  - Buscar por ID
  - USUÁRIO só vê sua própria empresa

- [x] `ListEmpresasUseCase`
  - Listar todas (apenas ADMIN)
  - **Paginação com Pageable** (page, size, sort)
  - **Retornar Page<EmpresaResponse>**

- [x] `DeleteEmpresaUseCase`
  - **Soft delete: set ativo = false** (apenas ADMIN, via @SQLDelete automático)
  - Audit log (DELETAR_EMPRESA)

### 6.3 - Use Cases de Usuario

- [x] `CreateUsuarioUseCase`
  - **Verificar se usuário autenticado é ADMIN** (somente ADMIN pode criar usuários)
  - **Verificar se nome (username) já existe** → lança `DuplicateUsernameException`
  - Criptografar senha (BCrypt strength 12)
  - Verificar se empresa existe
  - Salvar usuário
  - **Registrar audit log (CRIAR_USUARIO)**

- [x] `UpdateUsuarioUseCase`
  - Validar existência
  - USUARIO só atualiza seus próprios dados; ADMIN atualiza qualquer um
  - Atualizar senha se informada (criptografar)
  - Audit log (ATUALIZAR_USUARIO)

- [x] `GetUsuarioUseCase`
  - Buscar por ID
  - USUARIO vê apenas usuários da mesma empresa

- [x] `ListUsuariosUseCase`
  - ADMIN vê todos; USUARIO filtra pela sua empresa
  - **Paginação com Pageable** (page, size, sort)
  - **Retornar Page<UsuarioResponse>**

- [x] `DeleteUsuarioUseCase`
  - **Soft delete: set ativo = false** (via @SQLDelete automático)
  - Restrito a ADMIN
  - Audit log (DELETAR_USUARIO)

### 6.4 - Use Cases de Produto

- [x] `CreateProdutoUseCase`
  - USUARIO herda idEmpresa do token; ADMIN pode especificar
  - Verificar duplicidade de codigoEan dentro da mesma empresa
  - Salvar produto
  - Audit log (CRIAR_PRODUTO)

- [x] `UpdateProdutoUseCase`
  - Verificar existência e permissão de empresa
  - Atualizar produto
  - Audit log (ATUALIZAR_PRODUTO)

- [x] `GetProdutoUseCase`
  - Buscar por ID
  - Verificar permissão de empresa para não-admins

- [x] `ListProdutosUseCase`
  - ADMIN vê todos; USUARIO filtra pela sua empresa
  - **Paginação com Pageable** (page, size, sort)
  - **Retornar Page<ProdutoResponse>**

- [x] `DeleteProdutoUseCase`
  - **Soft delete: set ativo = false** (via @SQLDelete automático)
  - Verificar permissão de empresa
  - Audit log (DELETAR_PRODUTO)

### 6.5 - Use Cases de Imagem

- [x] `CreateImagemUseCase`
  - Verifica se produto existe
  - **Comprimir/redimensionar imagem opcionalmente** (Thumbnailator, skip abaixo de 1MB)
  - **Salvar registro com status PENDENTE** (antes do upload)
  - Upload para Cloudflare
  - **Atualizar status para ATIVO** se upload bem-sucedido
  - **Atualizar status para ERRO** se upload falhar (permite retry)
  - Audit log (UPLOAD_IMAGEM)
  - **@CacheEvict("imagensPorEan")** ao criar

- [x] `UpdateImagemUseCase`
  - Atualizar tipo de armazenamento
  - **@CacheEvict("imagensPorEan")** ao atualizar

- [x] `GetImagemByIdUseCase`
  - Buscar por ID
  - Imagem PRIVADA requer mesma empresa ou ADMIN

- [x] `GetImagensByCodigoEanUseCase`
  - Filtrar imagens com tipoArmazenamento=ABERTO e status=ATIVO
  - **@Cacheable("imagensPorEan")** — cache Caffeine para consultas frequentes
  - **Endpoint público** (sem autenticação JWT)

- [x] `GetImagensByProdutoUseCase`
  - Buscar por ID do produto (valida existência e permissão)
  - **Paginação com Pageable**

- [x] `DeleteImagemUseCase`
  - Tenta deletar da Cloudflare (fail-safe)
  - **Soft delete no banco: set ativo = false** (via @SQLDelete automático)
  - Verificar permissões
  - **@CacheEvict("imagensPorEan")** ao deletar
  - Audit log (DELETAR_IMAGEM)

### 6.6 - Use Cases de Segurança / Auditoria

- [x] `AuditLogUseCase` — **implementado inline em todos os use cases** via `IAuditLogRepository`
  - Ações auditadas: LOGIN, LOGOUT, LOGIN_FALHA, CRIAR_USUARIO, DELETAR_USUARIO, UPLOAD_IMAGEM, DELETAR_IMAGEM, CRIAR/ATUALIZAR/DELETAR EMPRESA/PRODUTO

- [x] `RefreshTokenUseCase` — implementado na Task 05 (Token rotation)

- [x] `LogoutUseCase` — implementado na Task 05 (blacklist + revogação)

### 6.7 - Mappers
- [x] Mappers com métodos DTO ↔ Domain Entity usando **MapStruct** (compile-time)
  - [x] `EmpresaMapper` — `toDomain`, `toEntity`, `toResponse`, `fromRequest`
  - [x] `UsuarioMapper` — `toDomain`, `toEntity`, `toResponse`, `fromRequest`
  - [x] `ProdutoMapper` — `toDomain`, `toEntity`, `toResponse`, `fromRequest`
  - [x] `ImagemMapper` — `toDomain`, `toEntity`, `toResponse`, `toProdutoResponse`
  - [x] `AuditLogMapper` — `toDomain`, `toEntity` (Task 03)
  - [x] `RefreshTokenMapper` — `toDomain`, `toEntity` (Task 03)

### 6.8 - Validações de Negócio
- [x] Validar regras de acesso por empresa (USUARIO filtra por idEmpresa do token)
- [x] Validar permissões de ADMIN vs USUARIO em todos os use cases
- [x] Validar integridade referencial (empresa existe antes de criar usuário; produto existe antes de criar imagem)
- [x] Bean Validation nos DTOs (`@NotBlank`, `@NotNull`, `@Size`)

## Critérios de Aceite
- [x] Todos os use cases implementados
- [x] Validações de negócio funcionando
- [x] Mappers com métodos DTO ↔ domínio (**MapStruct** compile-time)
- [x] Regras de permissão aplicadas corretamente
- [x] Upload de imagens integrado em `CreateImagemUseCase`
- [x] **Audit log registrando todas as ações sensíveis**
- [x] **CreateUsuarioUseCase restrito a ADMIN**
- [x] **RefreshTokenUseCase e LogoutUseCase funcionando** (Task 05)
- [x] **Cache @Cacheable/@CacheEvict funcionando para consultas por EAN**
- [x] **Paginação padronizada (Pageable) em todos os endpoints de listagem**
- [x] **Compressão de imagem opcional antes do upload (Thumbnailator)**
- [x] **Soft delete (ativo = false) em todos os delete use cases**
- [x] **Status da imagem (PENDENTE → ATIVO/ERRO) gerenciado no upload**
- [x] **Validação de username único no CreateUsuarioUseCase**
- [x] **Consulta por EAN filtra apenas imagens com status ATIVO**
- [x] **Endpoint EAN público (sem autenticação)**
- [x] BUILD SUCCESS confirmado
