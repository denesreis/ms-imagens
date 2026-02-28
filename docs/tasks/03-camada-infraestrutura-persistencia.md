# Task 03 - Camada de Infraestrutura - Persistência

## Objetivo
Implementar a camada de persistência com JPA/Hibernate

## Subtasks

### 3.1 - Criar Entidades JPA

#### 3.1.1 - UsuarioEntity (JPA)
- [x] Criar `UsuarioEntity` com anotações JPA
- [x] Mapear para tabela `tb_usuario`
- [x] Configurar relacionamentos (@ManyToOne com Empresa)
- [x] Adicionar índices necessários
- [x] Configurar auditoria (@CreatedDate, @LastModifiedDate)
- [x] Usar **Lombok:** @Entity, @Table, @Data, @Builder, @NoArgsConstructor, @AllArgsConstructor
- [x] Campo senha com length=60 para BCrypt
- [x] **Campo `nome` com `@Column(unique = true)`** - constraint de unicidade para login
- [x] **Campo `tentativas_login` (Integer, default 0)** - contador de falhas consecutivas
- [x] **Campo `bloqueado_ate` (LocalDateTime, nullable)** - data/hora de desbloqueio
- [x] **Campo `ativo` BOOLEAN DEFAULT TRUE** - soft delete
- [x] **`@SQLRestriction("ativo = true")`** - filtro automático soft delete (Hibernate 6.x)
- [x] **`@SQLDelete(sql = "UPDATE tb_usuario SET ativo = false WHERE id = ?")`** - soft delete

#### 3.1.2 - EmpresaEntity (JPA)
- [x] Criar `EmpresaEntity` com anotações JPA
- [x] Mapear para tabela `tb_empresa`
- [x] Adicionar índice único em `codigoErp`
- [x] Configurar auditoria
- [x] Usar **Lombok:** @Entity, @Table, @Data, @Builder, @NoArgsConstructor, @AllArgsConstructor
- [x] **Campo `ativo` BOOLEAN DEFAULT TRUE** - soft delete
- [x] **`@SQLRestriction("ativo = true")`** - filtro automático soft delete (Hibernate 6.x)
- [x] **`@SQLDelete(sql = "UPDATE tb_empresa SET ativo = false WHERE id = ?")`** - soft delete

#### 3.1.3 - ProdutoEntity (JPA)
- [x] Criar `ProdutoEntity` com anotações JPA
- [x] Mapear para tabela `tb_produto`
- [x] Configurar relacionamentos (@ManyToOne com Empresa)
- [x] Adicionar índice em `codigoEan`
- [x] Configurar auditoria
- [x] Usar UUID como ID (@GeneratedValue strategy UUID)
- [x] Usar **Lombok:** @Entity, @Table, @Data, @Builder, @NoArgsConstructor, @AllArgsConstructor
- [x] **Campo `ativo` BOOLEAN DEFAULT TRUE** - soft delete
- [x] **`@SQLRestriction("ativo = true")`** - filtro automático soft delete (Hibernate 6.x)
- [x] **`@SQLDelete(sql = "UPDATE tb_produto SET ativo = false WHERE id = ?")`** - soft delete

#### 3.1.4 - ImagemEntity (JPA)
- [x] Criar `ImagemEntity` com anotações JPA
- [x] Mapear para tabela `tb_imagem`
- [x] Configurar relacionamentos (@ManyToOne com Produto e Empresa)
- [x] Adicionar índices necessários
- [x] Configurar auditoria
- [x] Usar UUID como ID (@GeneratedValue strategy UUID)
- [x] Usar **Lombok:** @Entity, @Table, @Data, @Builder, @NoArgsConstructor, @AllArgsConstructor
- [x] Campo url como @Column(columnDefinition = "TEXT") para PostgreSQL
- [x] **Campo `status` (StatusImagemEnum, default PENDENTE)** - @Enumerated(EnumType.STRING) @Column(length = 10)
- [x] **Campo `ativo` BOOLEAN DEFAULT TRUE** - soft delete
- [x] **`@SQLRestriction("ativo = true")`** - filtro automático soft delete (Hibernate 6.x)
- [x] **`@SQLDelete(sql = "UPDATE tb_imagem SET ativo = false WHERE id = ?")`** - soft delete

#### 3.1.5 - RefreshTokenEntity (JPA)
- [x] Criar `RefreshTokenEntity` com anotações JPA
- [x] Mapear para tabela `tb_refresh_token`
- [x] Configurar relacionamento (@ManyToOne com Usuario)
- [x] Adicionar índice único em `token_hash`
- [ ] Campos:
  - id (Long, @GeneratedValue IDENTITY)
  - idUsuario (Long, FK para tb_usuario)
  - tokenHash (String, max 255) - **nunca armazenar token plain**
  - expiraEm (LocalDateTime)
  - revogado (Boolean, default false)
  - dataCriacao (LocalDateTime, @CreatedDate)
- [x] Usar **Lombok:** @Entity, @Table, @Data, @Builder, @NoArgsConstructor, @AllArgsConstructor

#### 3.1.6 - AuditLogEntity (JPA)
- [x] Criar `AuditLogEntity` com anotações JPA
- [x] Mapear para tabela `tb_audit_log`
- [x] Adicionar índices em: `acao`, `usuario`, `data_criacao`
- [ ] Campos:
  - id (Long, @GeneratedValue IDENTITY)
  - acao (String, max 50) - LOGIN, LOGOUT, LOGIN_FALHA, CRIAR_USUARIO, DELETAR_IMAGEM, etc.
  - usuario (String, max 30)
  - idEmpresa (Long, nullable)
  - ip (String, max 45) - suportar IPv4 e IPv6
  - detalhes (Text, nullable)
  - dataCriacao (LocalDateTime, @CreatedDate)
- [x] Usar **Lombok:** @Entity, @Table, @Data, @Builder, @NoArgsConstructor, @AllArgsConstructor

### 3.2 - Criar Repositórios JPA
- [x] `UsuarioJpaRepository extends JpaRepository<UsuarioEntity, Long>`
  - `Optional<UsuarioEntity> findByNome(String nome)`
  - `Page<UsuarioEntity> findByIdEmpresa(Long idEmpresa, Pageable pageable)` - **paginação**
  
- [x] `EmpresaJpaRepository extends JpaRepository<EmpresaEntity, Long>`
  - `Optional<EmpresaEntity> findByCodigoErp(String codigoErp)`
  
- [x] `ProdutoJpaRepository extends JpaRepository<ProdutoEntity, String>`
  - `List<ProdutoEntity> findByCodigoEan(String codigoEan)`
  - `Page<ProdutoEntity> findByIdEmpresa(Long idEmpresa, Pageable pageable)` - **paginação**
  
- [x] `ImagemJpaRepository extends JpaRepository<ImagemEntity, String>`
  - `Page<ImagemEntity> findByIdProduto(String idProduto, Pageable pageable)` - **paginação**
  - Query customizada para buscar por codigoEan com tipo aberto

- [x] `RefreshTokenJpaRepository extends JpaRepository<RefreshTokenEntity, Long>`
  - `Optional<RefreshTokenEntity> findByTokenHash(String tokenHash)`
  - `@Modifying void revokeAllByIdUsuario(Long idUsuario)` - revoga todos os tokens do usuário
  - `@Modifying void deleteByExpiraEmBefore(LocalDateTime now)` - limpa tokens expirados

- [x] `AuditLogJpaRepository extends JpaRepository<AuditLogEntity, Long>`
  - `List<AuditLogEntity> findByUsuario(String usuario)`
  - `List<AuditLogEntity> findByAcao(String acao)`
  - `List<AuditLogEntity> findByDataCriacaoBetween(LocalDateTime inicio, LocalDateTime fim)`

### 3.3 - Criar Adapters de Repositório
Implementar as interfaces de domínio usando os repositórios JPA:

- [x] `UsuarioRepositoryAdapter implements IUsuarioRepository`
- [x] `EmpresaRepositoryAdapter implements IEmpresaRepository`
- [x] `ProdutoRepositoryAdapter implements IProdutoRepository`
- [x] `ImagemRepositoryAdapter implements IImagemRepository`
- [x] `RefreshTokenRepositoryAdapter implements IRefreshTokenRepository`
- [x] `AuditLogRepositoryAdapter implements IAuditLogRepository`

Cada adapter deve:
- Converter entre entidades de domínio e JPA
- Usar **MapStruct** (mapeamento compile-time, obrigatório)
- Tratar exceções de persistência

### 3.4 - Scripts de Banco de Dados (Flyway)
- [x] Criar estrutura de migrations: `src/main/resources/db/migration/`
- [x] Criar migration inicial: **V1__create_tables.sql**
  - tb_empresa (**ativo BOOLEAN DEFAULT TRUE**, data_criacao TIMESTAMP, data_atualizacao TIMESTAMP)
  - tb_usuario (tentativas_login INTEGER DEFAULT 0, bloqueado_ate TIMESTAMP, **ativo BOOLEAN DEFAULT TRUE**, data_criacao, data_atualizacao, **UNIQUE(nome)**)
  - tb_produto (**ativo BOOLEAN DEFAULT TRUE**, data_criacao TIMESTAMP, data_atualizacao TIMESTAMP)
  - tb_imagem (**ativo BOOLEAN DEFAULT TRUE**, **status VARCHAR(10) DEFAULT 'PENDENTE'**, data_criacao TIMESTAMP, data_atualizacao TIMESTAMP)
  - **tb_refresh_token** (id, id_usuario FK, token_hash UNIQUE, expira_em, revogado, data_criacao)
  - **tb_audit_log** (id, acao, usuario, id_empresa, ip, detalhes TEXT, data_criacao)
  - Índices e constraints
  - **Índices adicionais:** idx_refresh_token_hash, idx_refresh_token_usuario, idx_audit_log_acao, idx_audit_log_usuario, idx_audit_log_data_criacao, **idx_usuario_nome (UNIQUE)**, **idx_imagem_status**
- [x] Criar migration de dados iniciais (opcional): **V2__insert_initial_data.sql**
  - Empresa padrão
  - Usuário administrador
- [x] Configurar Flyway properties no application.yml
- [ ] Testar migrations (clean + migrate)

### 3.5 - Configuração de Auditoria
- [x] Configurar `@EnableJpaAuditing`
- [x] Criar `AuditingConfig`
- [x] Implementar `AuditorAware<String>` se necessário

## Critérios de Aceite
- [x] Entidades JPA mapeadas corretamente com **Hibernate**
- [ ] Repositórios JPA funcionando com **PostgreSQL**
- [x] Adapters implementados com conversão de entidades
- [x] **Flyway migrations** criadas e executando corretamente
- [ ] Schema do banco criado automaticamente via Flyway
- [ ] Testes de repositório passando
- [x] Lombok funcionando (@Data, @Builder, @NoArgsConstructor, @AllArgsConstructor)
- [x] **Tabela tb_refresh_token criada e funcional**
- [x] **Tabela tb_audit_log criada e funcional**
- [x] **Campos de brute force (tentativas_login, bloqueado_ate) na tb_usuario**
- [x] **Soft delete com @SQLRestriction/@SQLDelete em todas as entidades principais**
- [x] **Campo ativo BOOLEAN DEFAULT TRUE em todas as tabelas (tb_empresa, tb_usuario, tb_produto, tb_imagem)**
- [x] **UNIQUE constraint em tb_usuario.nome**
- [x] **Campo status (PENDENTE, ATIVO, ERRO) na tb_imagem**
