# Task 02 - Camada de Domínio

## Objetivo
Implementar as entidades de domínio e contratos de repositórios (interfaces)

## Subtasks

### 2.1 - Criar Enums
- [x] `RoleEnum` (ADMINISTRADOR, USUARIO)
- [x] `TipoArmazenamentoEnum` (ABERTO, PRIVADO)
- [x] **`StatusImagemEnum` (PENDENTE, ATIVO, ERRO)** - status do upload na Cloudflare

### 2.2 - Criar Entidades de Domínio

#### 2.2.1 - Entidade Usuario
- [x] Criar `Usuario` domain entity
  - id (Long)
  - idEmpresa (Long)
  - nome (String, max 30) - **UNIQUE** (usado como username para login)
  - senha (String, max 60) - hash BCrypt (precisa mais espaço)
  - role (RoleEnum)
  - ativo (Boolean)
  - **tentativasLogin (Integer, default 0)** - contador de falhas de login
  - **bloqueadoAte (LocalDateTime, nullable)** - data/hora até quando a conta está bloqueada
  - dataCriacao (LocalDateTime)
  - dataAtualizacao (LocalDateTime)
- [x] Usar **Lombok anotações:**
  - @Data (getters, setters, toString, equals, hashCode)
  - @Builder (padrão builder)
  - @NoArgsConstructor e @AllArgsConstructor
- [x] Adicionar validações de negócio
- [x] Criar métodos de domínio (se necessário)

#### 2.2.2 - Entidade Empresa
- [x] Criar `Empresa` domain entity
  - id (Long)
  - codigoErp (String, max 20)
  - nome (String, max 100)
  - ativo (Boolean)
  - dataCriacao (LocalDateTime)
  - dataAtualizacao (LocalDateTime)
- [x] Usar **Lombok:** @Data, @Builder, @NoArgsConstructor, @AllArgsConstructor

#### 2.2.3 - Entidade Produto
- [x] Criar `Produto` domain entity
  - id (String/UUID)
  - descricao (String, max 80)
  - discriminacao (String, max 200)
  - codigoErp (String, max 20)
  - idEmpresa (Long, opcional)
  - codigoEan (String, max 13)
  - ativo (Boolean)
  - dataCriacao (LocalDateTime)
  - dataAtualizacao (LocalDateTime)
- [x] Usar **Lombok:** @Data, @Builder, @NoArgsConstructor, @AllArgsConstructor

#### 2.2.4 - Entidade Imagem
- [x] Criar `Imagem` domain entity
  - id (String/UUID)
  - idProduto (String)
  - idEmpresa (Long, opcional)
  - tipoArmazenamento (TipoArmazenamentoEnum)
  - idImagemCloudflare (String, max 50)
  - url (String/Text)
  - filename (String)
  - **status (StatusImagemEnum, default PENDENTE)** - rastreamento do upload (PENDENTE → ATIVO/ERRO)
  - ativo (Boolean)
  - dataCriacao (LocalDateTime)
  - dataAtualizacao (LocalDateTime)
- [x] Usar **Lombok:** @Data, @Builder, @NoArgsConstructor, @AllArgsConstructor

#### 2.2.5 - Entidade RefreshToken
- [x] Criar `RefreshToken` domain entity
  - id (Long)
  - idUsuario (Long)
  - tokenHash (String, max 255) - hash do refresh token (nunca armazenar token plain)
  - expiraEm (LocalDateTime)
  - revogado (Boolean, default false)
  - dataCriacao (LocalDateTime)
- [x] Usar **Lombok:** @Data, @Builder, @NoArgsConstructor, @AllArgsConstructor

#### 2.2.6 - Entidade AuditLog
- [x] Criar `AuditLog` domain entity
  - id (Long)
  - acao (String, max 50) - ex: LOGIN, LOGOUT, LOGIN_FALHA, CRIAR_USUARIO, DELETAR_IMAGEM
  - usuario (String, max 30) - nome do usuário que executou
  - idEmpresa (Long, opcional)
  - ip (String, max 45) - IPv4 ou IPv6
  - detalhes (String/Text, opcional) - informações adicionais
  - dataCriacao (LocalDateTime)
- [x] Usar **Lombok:** @Data, @Builder, @NoArgsConstructor, @AllArgsConstructor

### 2.3 - Criar Interfaces de Repositório (Domain)
- [x] `IUsuarioRepository`
- [x] `IEmpresaRepository`
- [x] `IProdutoRepository`
- [x] `IImagemRepository`
- [x] `IRefreshTokenRepository`
- [x] `IAuditLogRepository`

Métodos comuns em cada repositório:
- findById
- findAll (**filtrar por ativo = true** - soft delete automático)
- save
- deleteById → **soft delete** (set ativo = false, não deletar fisicamente)
- existsById

Métodos específicos:
- `IUsuarioRepository`: findByNome, findByIdEmpresa
- `IEmpresaRepository`: findByCodigoErp
- `IProdutoRepository`: findByCodigoEan, findByIdEmpresa
- `IImagemRepository`: findByIdProduto, findByCodigoEanAndTipoAberto
- `IRefreshTokenRepository`: findByTokenHash, revokeAllByUsuarioId, deleteExpired
- `IAuditLogRepository`: findByUsuario, findByAcao, findByDateRange

### 2.4 - Criar Exceções de Domínio
- [x] `BusinessException` (base)
- [x] `ResourceNotFoundException`
- [x] `UnauthorizedException`
- [x] `InvalidDataException`
- [x] `CloudflareException`
- [x] `AccountLockedException` - conta bloqueada por tentativas excessivas de login
- [x] `TokenRevokedException` - token já foi revogado/invalidado
- [x] **`DuplicateUsernameException`** - nome de usuário já existe (unique constraint)

## Critérios de Aceite
- [x] Todas as entidades criadas com validações
- [x] Interfaces de repositório definidas
- [x] Código sem dependências de frameworks na camada de domínio
- [x] Exceções de negócio implementadas
- [x] **StatusImagemEnum definido (PENDENTE, ATIVO, ERRO)**
- [x] **Campo status na entidade Imagem**
- [x] **Campo nome UNIQUE na entidade Usuario**
- [x] **Soft delete (ativo = false) previsto nas interfaces de repositório**
