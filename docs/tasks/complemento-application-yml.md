# Complemento - Configuração do application.yml

## Configuração Completa dos Profiles

Este arquivo complementa a **Task 01** com as configurações específicas do application.yml.

---

## application.yml (Base)

```yaml
spring:
  application:
    name: MS-BlueDot
  
  # Profile ativo (dev, test, prod)
  profiles:
    active: ${SPRING_PROFILE:dev}
  
  # JPA / Hibernate
  jpa:
    open-in-view: false
    show-sql: false
    properties:
      hibernate:
        format_sql: true
        use_sql_comments: true
        jdbc:
          batch_size: 20
        order_inserts: true
        order_updates: true
  
  # Jackson
  jackson:
    serialization:
      write-dates-as-timestamps: false
    time-zone: America/Sao_Paulo
    default-property-inclusion: non_null
  
  # Multipart (upload de arquivos)
  servlet:
    multipart:
      enabled: true
      max-file-size: 10MB
      max-request-size: 20MB
  
  # NOTA: NÃO usar spring.security.user - autenticação 100% via JWT

# Configurações do Servidor
server:
  port: ${SERVER_PORT:8080}
  servlet:
    context-path: /api/v1   # Versionamento de API via context-path
  error:
    include-message: always
    include-binding-errors: always
    include-stacktrace: on_param
    include-exception: false
  compression:
    enabled: true
    mime-types: application/json,application/xml,text/html,text/xml,text/plain

# Configurações da Aplicação
app:
  jwt:
    secret: ${JWT_SECRET:minha-chave-secreta-super-segura-deve-ter-no-minimo-256-bits}
    expiration: ${JWT_EXPIRATION:1800000} # 30 minutos em milissegundos
    refresh-expiration: ${JWT_REFRESH_EXPIRATION:604800000} # 7 dias em milissegundos
  
  security:
    login:
      max-attempts: ${LOGIN_MAX_ATTEMPTS:5}          # bloqueio após 5 tentativas
      block-duration: ${LOGIN_BLOCK_DURATION:900000}  # 15 minutos em milissegundos
  
  cloudflare:
    account-id: ${CLOUDFLARE_ACCOUNT_ID}
    base-url: https://api.cloudflare.com/client/v4
    get-token: ${CLOUDFLARE_GET_TOKEN}
    post-token: ${CLOUDFLARE_POST_TOKEN}
    max-file-size: 10485760 # 10MB em bytes
    allowed-extensions: jpg,jpeg,png,gif,webp
    timeout:
      connect: 10000 # 10 segundos
      read: 30000    # 30 segundos
  
  image:
    compression:
      enabled: ${IMAGE_COMPRESSION_ENABLED:true}
      max-width: ${IMAGE_MAX_WIDTH:1920}
      max-height: ${IMAGE_MAX_HEIGHT:1080}
      quality: ${IMAGE_QUALITY:0.85}

# Cache (Caffeine)
spring.cache:
  type: caffeine
  cache-names: imagensPorEan,imagensPorProduto
  caffeine:
    spec: maximumSize=500,expireAfterWrite=10m

# Actuator (Monitoramento)
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
      base-path: /actuator
  endpoint:
    health:
      show-details: when-authorized
  metrics:
    export:
      prometheus:
        enabled: true

# Logging
logging:
  level:
    root: INFO
    com.empresa.msbluedot: INFO
    org.springframework.web: INFO
    org.springframework.security: INFO
    org.hibernate.SQL: INFO
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"

# Springdoc OpenAPI
springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /swagger-ui.html
    enabled: true
    operations-sorter: method
    tags-sorter: alpha
  show-actuator: false
```

---

## application-dev.yml (Desenvolvimento)

```yaml
spring:
  # PostgreSQL (Local)
  datasource:
    url: jdbc:postgresql://localhost:5432/msbluedot
    username: ${DB_USER:postgres}
    password: ${DB_PASSWORD:postgres}
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      connection-timeout: 20000
      idle-timeout: 300000
      max-lifetime: 1200000
  
  # JPA
  jpa:
    hibernate:
      ddl-auto: validate # Flyway gerencia o schema
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.PostgreSQLDialect
  
  # Flyway
  flyway:
    enabled: true
    baseline-on-migrate: true
    locations: classpath:db/migration
    validate-on-migrate: true

# Logging detalhado
logging:
  level:
    com.empresa.msbluedot: DEBUG
    org.springframework.web: DEBUG
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE

# Swagger habilitado
springdoc:
  swagger-ui:
    enabled: true

# Actuator aberto (DEV only!)
management:
  endpoint:
    health:
      show-details: always
```

---

## application-test.yml (Testes)

```yaml
spring:
  # H2 em memória para testes rápidos
  datasource:
    url: jdbc:h2:mem:testdb;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE
    username: sa
    password:
    driver-class-name: org.h2.Driver
  
  # JPA
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.H2Dialect
  
  # H2 Console (para debug)
  h2:
    console:
      enabled: true
      path: /h2-console
  
  # Flyway desabilitado (H2 cria schema)
  flyway:
    enabled: false

# Logging mínimo
logging:
  level:
    root: WARN
    com.empresa.msbluedot: INFO

# Cloudflare mock (não chamar API real nos testes)
app:
  cloudflare:
    base-url: http://localhost:8888/mock

# Swagger desabilitado
springdoc:
  swagger-ui:
    enabled: false
```

---

## application-prod.yml (Produção)

```yaml
spring:
  # PostgreSQL (Produção - via variáveis de ambiente)
  datasource:
    url: ${DATABASE_URL:jdbc:postgresql://localhost:5432/msbluedot}
    username: ${DB_USER}
    password: ${DB_PASSWORD}
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 20
      minimum-idle: 10
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
      leak-detection-threshold: 60000
  
  # JPA
  jpa:
    hibernate:
      ddl-auto: validate # NUNCA usar create/update em produção!
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        jdbc:
          batch_size: 50
  
  # Flyway
  flyway:
    enabled: true
    baseline-on-migrate: false
    locations: classpath:db/migration
    validate-on-migrate: true
    out-of-order: false

# Logging otimizado
logging:
  level:
    root: WARN
    com.empresa.msbluedot: INFO
    org.springframework: WARN
  file:
    name: /var/log/MS-BlueDot/application.log
    max-size: 10MB
    max-history: 30

# Swagger protegido ou desabilitado
springdoc:
  swagger-ui:
    enabled: ${SWAGGER_ENABLED:false}

# Actuator restrito
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: when-authorized

# JWT obrigatório via env
app:
  jwt:
    secret: ${JWT_SECRET} # OBRIGATÓRIO via variável de ambiente
    expiration: ${JWT_EXPIRATION:1800000}
  
  cloudflare:
    account-id: ${CLOUDFLARE_ACCOUNT_ID}
    get-token: ${CLOUDFLARE_GET_TOKEN}
    post-token: ${CLOUDFLARE_POST_TOKEN}
```

---

## Exemplo de .env (para desenvolvimento local)

Criar arquivo `.env` na raiz do projeto (NÃO commitar!):

```bash
# Database
DB_USER=postgres
DB_PASSWORD=postgres
DATABASE_URL=jdbc:postgresql://localhost:5432/msbluedot

# JWT
JWT_SECRET=minha-chave-secreta-super-segura-deve-ter-no-minimo-256-bits-para-HS512
JWT_EXPIRATION=1800000

# Cloudflare
CLOUDFLARE_ACCOUNT_ID=8e858ade7b52abbd9f51c3071f0cbf42
CLOUDFLARE_GET_TOKEN=uiXHR6nRCvOmsEuJ7Rm0Pv530NS7A_QaYr38KxbN
CLOUDFLARE_POST_TOKEN=zXnfAoOiLUHdFJhLxjhJW7c9nGxlxX3BduO2IX2Y

# Server
SERVER_PORT=8080
SPRING_PROFILE=dev

# Swagger (prod)
SWAGGER_ENABLED=false
```

---

## Classe de Configuração para Properties

```java
package com.empresa.msbluedot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.cloudflare")
public class CloudflareProperties {
    private String accountId;
    private String baseUrl;
    private String getToken;
    private String postToken;
    private Long maxFileSize;
    private String allowedExtensions;
    private Timeout timeout;
    
    @Data
    public static class Timeout {
        private Integer connect;
        private Integer read;
    }
}
```

```java
package com.empresa.msbluedot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {
    private String secret;
    private Long expiration;
}
```

---

## Configuração do Flyway

Estrutura de pastas:
```
src/main/resources/
└── db/
    └── migration/
        ├── V1__create_tables.sql
        ├── V2__insert_initial_data.sql
        └── V3__add_indexes.sql
```

**Regras de nomenclatura:**
- `V{versão}__{descrição}.sql`
- Versão: número sequencial (1, 2, 3...)
- Descrição: snake_case

---

## Security Headers (adicionar em SecurityConfig)

```java
http.headers()
    .contentSecurityPolicy("default-src 'self'")
    .and()
    .frameOptions().deny()
    .and()
    .xssProtection().block(true)
    .and()
    .contentTypeOptions();
```

---

## Observações Importantes

### 1. Variáveis de Ambiente Obrigatórias em Produção
- `JWT_SECRET`: Nunca usar valor padrão!
- `DB_USER` e `DB_PASSWORD`: Credenciais do banco
- `CLOUDFLARE_GET_TOKEN` e `CLOUDFLARE_POST_TOKEN`: Tokens da API

### 2. Profiles
- **dev:** Desenvolvimento local, logs detalhados
- **test:** Testes automatizados, H2 em memória
- **prod:** Produção, logs otimizados, segurança máxima

### 3. Flyway
- Sempre usar `ddl-auto: validate` com Flyway
- Migrations são imutáveis (não alterar após deploy)
- Testar migrations em ambiente de staging primeiro

### 4. Hikari Connection Pool
- **Dev:** Pool pequeno (5-10 conexões)
- **Prod:** Pool maior (10-20 conexões)
- Ajustar conforme carga esperada

### 5. Upload de Arquivos
- Limite padrão: 10MB por arquivo
- Ajustar conforme necessidade
- Validar no backend também!

---

**Próximo Passo:** Com pom.xml e application.yml configurados, iniciar Task 02 (Camada de Domínio)


