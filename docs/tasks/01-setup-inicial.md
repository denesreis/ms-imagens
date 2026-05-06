# Task 01 - Setup Inicial do Projeto

## Objetivo
Configurar a estrutura base do projeto Spring Boot seguindo boas práticas

## Subtasks

### 1.1 - Criar projeto Spring Boot
- [x] Criar projeto usando Spring Initializr:
  - **Spring Boot:** 3.4.3 (versão estável da linha 3.4.x)
  - **Java:** 21
  - **Packaging:** WAR
  - **Build Tool:** Maven
  
- [x] Adicionar dependências essenciais:
  - Spring Web
  - Spring Data JPA
  - Spring Security
  - Spring Validation
  - **Lombok** (obrigatório)
  - **PostgreSQL Driver** (org.postgresql:postgresql)
  - H2 Database (para testes)
  - JWT (jjwt-api, jjwt-impl, jjwt-jackson)
  - Springdoc OpenAPI (documentação)
  - WebClient (Spring WebFlux para Cloudflare API)
  - **MapStruct** (mapeamento compile-time - obrigatório) + mapstruct-processor
  - **Spring Boot Starter Cache** (spring-boot-starter-cache)
  - **Flyway Core** (db.migration - obrigatório)
  - **Caffeine** (com.github.ben-manes.caffeine:caffeine - cache para blacklist JWT e consultas)
  - **Thumbnailator** (net.coobird:thumbnailator - compressão de imagens antes do upload)

### 1.2 - Estrutura de pastas
- [x] Criar estrutura seguindo Clean Architecture:
```
src/main/java/com/empresa/msbluedot/
├── domain/
│   ├── entities/
│   ├── repositories/
│   ├── enums/
│   └── exceptions/
├── application/
│   ├── usecases/
│   ├── dto/
│   └── mappers/
├── infrastructure/
│   ├── controllers/
│   ├── persistence/
│   │   ├── entities/
│   │   └── repositories/
│   ├── external/
│   │   └── cloudflare/
│   └── security/
└── config/
```

### 1.3 - Arquivo de configuração
- [x] Configurar `application.yml` com profiles (dev, test, prod)
- [x] Configurar variáveis de ambiente:
  - `CLOUDFLARE_GET_TOKEN` (usar variável de ambiente - **NÃO versionar o valor real**)
  - `CLOUDFLARE_POST_TOKEN` (usar variável de ambiente - **NÃO versionar o valor real**)
  - `CLOUDFLARE_ACCOUNT_ID` (usar variável de ambiente - **NÃO versionar o valor real**)
  - `JWT_SECRET` (mínimo 256 bits - **NÃO versionar o valor real**)
  - `JWT_EXPIRATION` (1800000 = 30 minutos)
  - `JWT_REFRESH_EXPIRATION` (604800000 = 7 dias)
- [x] Configurar propriedades do **PostgreSQL**:
  - spring.datasource.url
  - spring.datasource.username
  - spring.datasource.password
  - spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
- [x] Configurar **Flyway**:
  - spring.flyway.enabled=true
  - spring.flyway.locations=classpath:db/migration
- [x] Configurar propriedades do servidor (porta, context-path)

### 1.4 - Configurações gerais
- [x] Criar `.gitignore` apropriado
- [x] Criar `README.md` com instruções
- [x] Configurar `pom.xml`:
  - **Java version:** 21
  - **Spring Boot version:** 3.4.3
  - **Packaging:** war
  - Properties do Maven Compiler (release=21)
  - Incluir `spring-boot-starter-tomcat` com scope provided
  - **Configurar annotation processors:** Lombok + MapStruct (mapstruct-processor)
- [x] Adicionar editorconfig para padronização de código
- [x] Configurar Lombok annotation processor
- [x] Configurar MapStruct annotation processor (com lombok-mapstruct-binding)

## Critérios de Aceite
- [x] Estrutura de pastas criada (Clean Architecture)
- [x] Packaging WAR configurado no pom.xml
- [x] Configurações básicas funcionando (application.yml + profiles dev/test/prod)
- [x] Variáveis de ambiente documentadas (.env.example)
- [x] Flyway configurado
- [x] **MapStruct + Lombok annotation processors configurados no pom.xml**
- [x] **Spring Boot 3.4.3 configurado**
- [x] Classes de configuração criadas (CloudflareProperties, JwtProperties, etc.)
- [x] Projeto compila sem erros (requer Maven instalado)
- [ ] Flyway rodando (requer PostgreSQL)


