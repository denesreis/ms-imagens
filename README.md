# MS BlueDot

Micro-serviço de gerenciamento de imagens de produtos integrado com **Cloudflare Images API**.

## Stack Tecnológica

| Tecnologia | Versão | Uso |
|---|---|---|
| Java | 21 | Linguagem principal |
| Spring Boot | 3.4.3 | Framework base |
| Packaging | WAR | Deploy em Tomcat 10+ |
| Maven | 3.9+ | Build tool |
| PostgreSQL | 16 | Banco de dados |
| Hibernate | (Spring Boot) | ORM / Persistência |
| Flyway | 10.10.0 | Migrations de banco |
| Lombok | 1.18.30 | Redução de boilerplate |
| MapStruct | 1.5.5 | Mapeamento compile-time |
| JJWT | 0.12.5 | Tokens JWT |
| Caffeine | 3.1.8 | Cache (blacklist JWT + EAN) |
| Thumbnailator | 0.4.20 | Compressão de imagens |
| Springdoc OpenAPI | 2.3.0 | Documentação API |

## Arquitetura

Segue **Clean Architecture** com separação em camadas:

```
src/main/java/com/scasistemas/msbluedot/
├── domain/              ← Entidades, contratos, exceções (sem frameworks)
├── application/         ← Use cases, DTOs, mappers (regras de negócio)
├── infrastructure/      ← Controllers, JPA, Cloudflare, Security
└── config/              ← Configurações Spring
```

## Pré-requisitos

- JDK 21
- Maven 3.9+
- PostgreSQL 16 (ou Docker)
- Conta na Cloudflare Images API

## Configuração

1. **Clone o repositório e entre na pasta:**
   ```bash
   git clone <repo-url>
   cd ms-bluedot
   ```

2. **Configure as variáveis de ambiente:**
   ```bash
   cp .env.example .env
   # Edite .env com os valores reais
   ```

3. **Crie o banco de dados:**
   ```sql
   CREATE DATABASE msbluedot;
   ```

4. **Execute as migrations:**
   ```bash
   mvn flyway:migrate
   ```

## Executar em Desenvolvimento

```bash
mvn spring-boot:run
```

A aplicação estará disponível em: `http://localhost:8080/api/v1`

Swagger UI: `http://localhost:8080/api/v1/swagger-ui.html`

## Gerar WAR para Deploy

```bash
mvn clean package -DskipTests
```

O arquivo WAR será criado em `target/ms-bluedot.war`.

Deploy em Tomcat 10+: copie o WAR para a pasta `webapps/`.

## Executar Testes

```bash
# Todos os testes
mvn test

# Com relatório de cobertura
mvn clean test jacoco:report

# Relatório em: target/site/jacoco/index.html
```

> **Nota:** Os testes utilizam H2 em memória (perfil `test`). Não é necessário Docker para rodar os testes.

## Profiles

| Profile | Banco | Uso |
|---|---|---|
| `dev` | PostgreSQL local | Desenvolvimento |
| `test` | H2 em memória | Testes unitários |
| `prod` | PostgreSQL via env vars | Produção |

## Variáveis de Ambiente Obrigatórias em Produção

| Variável | Descrição |
|---|---|
| `JWT_SECRET` | Secret JWT (mín. 256 bits) |
| `DB_USER` / `DB_PASSWORD` | Credenciais do banco |
| `DATABASE_URL` | URL de conexão PostgreSQL |
| `CLOUDFLARE_ACCOUNT_ID` | ID da conta Cloudflare |
| `CLOUDFLARE_GET_TOKEN` | Token de leitura Cloudflare |
| `CLOUDFLARE_POST_TOKEN` | Token de escrita Cloudflare |

## Endpoints Principais

| Método | Endpoint | Descrição | Auth |
|---|---|---|---|
| POST | `/api/v1/auth/login` | Login | Público |
| POST | `/api/v1/auth/refresh` | Renovar token | Público |
| POST | `/api/v1/auth/logout` | Logout | JWT |
| GET | `/api/v1/imagens/ean/{ean}` | Imagens por EAN | **Público** |
| GET/POST/PUT/DELETE | `/api/v1/empresas/**` | CRUD Empresas | JWT (ADMIN) |
| GET/POST/PUT/DELETE | `/api/v1/usuarios/**` | CRUD Usuários | JWT (ADMIN criar) |
| GET/POST/PUT/DELETE | `/api/v1/produtos/**` | CRUD Produtos | JWT |
| GET/POST/PUT/DELETE | `/api/v1/imagens/**` | CRUD Imagens | JWT |
| GET | `/api/v1/health` | Health check | Público |

## Segurança

- **JWT** com access token (30min) + refresh token (7 dias)
- **Blacklist** de tokens (Caffeine cache) para logout imediato
- **Brute force protection**: bloqueio após 5 tentativas por 15 minutos
- **BCrypt** strength 12 para senhas
- **Audit log** de ações sensíveis
- Secrets via **variáveis de ambiente** (nunca hardcoded)

## Task Progress

- [x] Task 01 - Setup Inicial
- [x] Task 02 - Camada de Domínio
- [x] Task 03 - Camada de Persistência
- [x] Task 04 - Integração Cloudflare
- [x] Task 05 - Segurança JWT
- [x] Task 06 - Use Cases
- [x] Task 07 - Controllers
- [x] Task 08 - Testes (198 testes, BUILD SUCCESS)
- [x] Task 09 - Documentação e Deploy
# ms-bluedot
# ms-bluedot

