# Resumo do Plano de Tasks - MS Imagens

## Visão Geral

Projeto de micro-serviço para gerenciamento de imagens integrado com Cloudflare Images API, seguindo Clean Architecture e boas práticas Spring Boot.

---

## Tasks Organizadas

### ✅ Task 01 - Setup Inicial (Fundação)
**Arquivo:** `01-setup-inicial.md`
- Criar projeto Spring Boot 3.4.3 (Java 21, WAR)
- Estrutura de pastas (Clean Architecture)
- Configurações e variáveis de ambiente
- Dependências essenciais (Lombok, PostgreSQL, Flyway, **MapStruct, Caffeine, Thumbnailator**)

**Estimativa:** 5 horas

---

### ✅ Task 02 - Camada de Domínio (Core Business)
**Arquivo:** `02-camada-dominio.md`
- Entidades de domínio com **Lombok** (Usuario, Empresa, Produto, Imagem, **RefreshToken, AuditLog**)
- Enums (Role, TipoArmazenamento, **StatusImagem**)
- Interfaces de repositório (incluindo **IRefreshTokenRepository, IAuditLogRepository**)
- Exceções de negócio (**AccountLockedException, TokenRevokedException, DuplicateUsernameException**)
- **Campos de brute force** em Usuario (tentativasLogin, bloqueadoAte)
- **Campo nome UNIQUE** em Usuario
- **Campo status** (PENDENTE, ATIVO, ERRO) em Imagem
- **Soft delete** previsto nas interfaces de repositório

**Estimativa:** 9 horas

---

### ✅ Task 03 - Persistência (Infraestrutura)
**Arquivo:** `03-camada-infraestrutura-persistencia.md`
- Entidades JPA com **Hibernate** e **Lombok** (incluindo **RefreshTokenEntity, AuditLogEntity**)
- Repositórios JPA para **PostgreSQL** (incluindo **RefreshTokenJpaRepository, AuditLogJpaRepository**)
- Adapters de repositório (6 adapters) com **MapStruct**
- **Flyway Migrations** com tabelas: tb_empresa, tb_usuario, tb_produto, tb_imagem, **tb_refresh_token, tb_audit_log**
- Auditoria
- **Repositórios com paginação (Pageable)**
- **Soft delete com @SQLRestriction/@SQLDelete** em todas as entidades
- **Campo ativo BOOLEAN DEFAULT TRUE** em todas as tabelas
- **UNIQUE constraint** em tb_usuario.nome
- **Campo status** (PENDENTE, ATIVO, ERRO) na tb_imagem

**Estimativa:** 12 horas

---

### ✅ Task 04 - Integração Cloudflare (Infraestrutura)
**Arquivo:** `04-integracao-cloudflare.md`
- Cliente WebClient para Cloudflare API
- Upload de imagens
- Verificação de token
- Tratamento de erros
- DTOs de integração
- **Compressão de imagem pré-upload (Thumbnailator)**
- **Gerenciamento de status da imagem** (PENDENTE → ATIVO/ERRO)

**Estimativa:** 9 horas

---

### ✅ Task 05 - Segurança e JWT (Infraestrutura)
**Arquivo:** `05-seguranca-jwt.md`
- JWT token provider (access token + **refresh token**)
- UserDetailsService (com **verificação de bloqueio**)
- Filtros de autenticação (com **blacklist check**)
- SecurityConfig
- Autorização por roles
- **RefreshTokenService** (persistência no banco, hash, revogação)
- **TokenBlacklistService** (cache Caffeine para logout)
- **LoginAttemptService** (proteção brute force: 5 tentativas, bloqueio 15min)
- **BCrypt strength 12**
- Use cases: AuthenticateUser, **RefreshToken, Logout**
- Endpoints: /auth/login, **/auth/refresh, /auth/logout**

**Estimativa:** 14 horas

---

### ✅ Task 06 - Use Cases (Aplicação)
**Arquivo:** `06-camada-aplicacao-usecases.md`
- DTOs de requisição/resposta
- Use cases de Empresa (CRUD)
- Use cases de Usuario (CRUD) - **CreateUsuario restrito a ADMIN**
- Use cases de Produto (CRUD + upload)
- Use cases de Imagem (CRUD + consultas especiais)
- **Use cases de Segurança/Auditoria** (AuditLogUseCase, RefreshTokenUseCase, LogoutUseCase)
- Mappers com **MapStruct** (compile-time)
- **Cache Caffeine** para consultas frequentes (@Cacheable/@CacheEvict)
- **Paginação padronizada** (Pageable) em todos os endpoints de listagem
- Validações de negócio
- **Soft delete** (ativo = false) em todos os delete use cases
- **Status da imagem** gerenciado no upload (PENDENTE → ATIVO/ERRO)
- **Validação de username único** no CreateUsuarioUseCase

**Estimativa:** 17 horas

---

### ✅ Task 07 - Controllers (Apresentação)
**Arquivo:** `07-camada-apresentacao-controllers.md`
- AuthController (login, **refresh, logout**)
- EmpresaController (CRUD) - **paginação**
- UsuarioController (CRUD - **POST restrito a ADMIN**) - **paginação**
- ProdutoController (CRUD + multipart) - **paginação**
- ImagemController (CRUD + consultas por EAN) - **paginação**
- HealthController
- Exception Handler global (**AccountLockedException → 423, TokenRevokedException → 401**)
- **Versionamento de API: /api/v1/**
- Validações

**Estimativa:** 14 horas

---

### ✅ Task 08 - Testes (Qualidade)
**Arquivo:** `08-testes.md`
- Testes unitários (domain, use cases)
- Testes de integração com **TestContainers PostgreSQL**
- Testes de segurança (**refresh token, blacklist, brute force, audit log**)
- **Testes de cache, paginação e compressão de imagem**
- **Testes de soft delete, status da imagem, unique constraint username, endpoint EAN público**
- Mocks da Cloudflare
- Cobertura de código (80%+ com JaCoCo)

**Estimativa:** 18 horas

---

### ✅ Task 09 - Documentação e Deploy (Entrega)
**Arquivo:** `09-documentacao-deploy.md`
- OpenAPI/Swagger
- README completo com stack tecnológico
- **Dockerfile WAR** + docker-compose (Tomcat 10 + PostgreSQL 16)
- CI/CD (GitHub Actions)
- Profiles (dev, test, prod)
- Monitoramento (Actuator)
- Collection Postman
- Segurança produção

**Estimativa:** 8 horas

---

## Resumo Executivo

| Task | Área | Estimativa | Prioridade |
|------|------|------------|------------|
| 01 | Setup | 5h | Alta |
| 02 | Domínio | 9h | Alta |
| 03 | Persistência | 12h | Alta |
| 04 | Cloudflare | 9h | Alta |
| 05 | Segurança | 14h | Alta |
| 06 | Use Cases | 17h | Alta |
| 07 | Controllers | 15h | Alta |
| 08 | Testes | 19h | Média |
| 09 | Deploy | 8h | Média |
| **TOTAL** | - | **107h** | - |

---

## Ordem de Execução Recomendada

### Fase 1 - Fundação (Tasks 01-02)
Estabelecer base do projeto e modelo de domínio
**Duração:** ~10 horas

### Fase 2 - Infraestrutura (Tasks 03-05)
Implementar persistência, integrações e segurança
**Duração:** ~22 horas

### Fase 3 - Aplicação (Tasks 06-07)
Implementar lógica de negócio e endpoints
**Duração:** ~22 horas

### Fase 4 - Qualidade e Entrega (Tasks 08-09)
Testes, documentação e preparação para produção
**Duração:** ~20 horas

---

## Tecnologias Principais

- **Framework:** Spring Boot 3.4.3
- **Linguagem:** Java 21
- **Packaging:** WAR
- **Arquitetura:** Clean Architecture / Hexagonal
- **Segurança:** Spring Security + JWT (access + refresh)
- **Banco de Dados:** PostgreSQL 16
- **Persistência:** Hibernate (Spring Data JPA)
- **Migrations:** Flyway
- **Biblioteca:** Lombok
- **Mapper:** MapStruct (compile-time)
- **Cache:** Caffeine (spring-boot-starter-cache)
- **Cliente HTTP:** WebClient (Spring WebFlux)
- **Documentação:** Springdoc OpenAPI
- **Compressão:** Thumbnailator
- **Testes:** JUnit 5 + Mockito + TestContainers PostgreSQL
- **Build:** Maven
- **Container:** Docker + Tomcat 10
- **Versionamento API:** /api/v1/

---

## Funcionalidades Principais

1. ✅ CRUD de Empresas
2. ✅ CRUD de Usuários (com autenticação)
3. ✅ CRUD de Produtos (com upload de múltiplas imagens)
4. ✅ CRUD de Imagens (integrado com Cloudflare)
5. ✅ Autenticação JWT (30min access token + **7 dias refresh token**)
6. ✅ Consulta de imagens por EAN (públicas)
7. ✅ Autorização por roles (ADMIN/USUARIO)
8. ✅ Upload multipart de arquivos
9. ✅ Integração completa com Cloudflare Images API
10. ✅ **Refresh Token** com rotação e persistência no banco
11. ✅ **Logout com blacklist JWT** (Caffeine cache)
12. ✅ **Proteção brute force** (bloqueio após 5 tentativas, 15min)
13. ✅ **Audit log** de ações sensíveis (login, logout, CRUD usuários, imagens)
14. ✅ **BCrypt strength 12** (padrão 2026)
15. ✅ **Criação de usuários restrita a ADMIN**
16. ✅ **Cache Caffeine** para consultas frequentes (imagens por EAN)
17. ✅ **MapStruct** para mapeamento compile-time
18. ✅ **Versionamento de API** (/api/v1/)
19. ✅ **Paginação padronizada** (Pageable) em todos os endpoints de listagem
20. ✅ **Compressão de imagem** antes do upload (Thumbnailator)
21. ✅ **Status da imagem** (PENDENTE, ATIVO, ERRO) com rastreamento de upload
22. ✅ **Soft delete** com @SQLRestriction/@SQLDelete em todas as entidades
23. ✅ **Unique constraint** no username (tb_usuario.nome)
24. ✅ **Endpoint EAN público** (sem autenticação JWT)

---

## Próximos Passos

1. Revisar e validar o plano com o time
2. Iniciar pela **Task 01** (Setup Inicial)
3. Realizar code review ao final de cada task
4. Manter documentação atualizada
5. Executar testes continuamente

---

## Observações Importantes

- **Clean Architecture:** Separação clara entre camadas (domain, application, infrastructure)
- **SOLID:** Aplicar princípios em todas as classes
- **Lombok:** Usar anotações para reduzir boilerplate (@Data, @Builder, etc.)
- **WAR Deployment:** Empacotamento WAR para deploy em Tomcat
- **Flyway Migrations:** Versionamento obrigatório de schema do banco
- **PostgreSQL:** Banco de dados relacional oficial do projeto
- **Java 21:** Aproveitar features modernas (Records, Pattern Matching, Virtual Threads se aplicável)
- **Security First:** JWT obrigatório, roles bem definidas
- **API First:** Documentação OpenAPI desde o início
- **Test Driven:** Mínimo 80% de cobertura
- **Cloud Ready:** Preparado para containerização

**Estimativa original:** 74h  
**Estimativa com segurança reforçada:** 92h (+18h)
**Estimativa com tecnologias aplicadas:** 101h (+9h adicionais)
**Estimativa com arquitetura aplicada:** **107h** (+6h adicionais)

---

**Última atualização:** 19/02/2026  
**Task 01 - Setup:** ✅ CONCLUÍDA
