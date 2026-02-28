# Task 09 - Documentação e Deploy

## Objetivo
Documentar a API e preparar para deploy em produção

## Subtasks

### 9.1 - Documentação da API (OpenAPI/Swagger)

- [x] Configurar Springdoc OpenAPI
- [x] Adicionar anotações nos controllers:
  - `@Tag` (agrupamento)
  - `@Operation` (descrição do endpoint)
  - `@ApiResponse` (respostas possíveis)
  - `@Parameter` (parâmetros)
  - `@Schema` (schemas dos DTOs)
  
- [x] Configurar informações gerais da API:
  - Título: "MS Imagens API"
  - Versão
  - Descrição
  - Contato
  
- [x] Configurar segurança JWT no Swagger:
  - Bearer Authentication
  - Botão "Authorize"
  
- [x] Acessível em: `/swagger-ui.html`

### 9.2 - README.md

- [x] Criar `README.md` completo com:
  - Descrição do projeto
  - **Tecnologias utilizadas:**
    - Spring Boot 3.4.3
    - Java 21
    - PostgreSQL 16
    - Hibernate
    - Lombok
    - Flyway
    - Maven
    - Packaging: WAR
  - Pré-requisitos (Java 21, Maven 3.9+, PostgreSQL 16, Docker)
  - Configuração de variáveis de ambiente
  - Como executar localmente
  - Como executar testes
  - Endpoints principais
  - Exemplos de uso
  - Estrutura do projeto
  - Diagramas (se aplicável)

### 9.3 - Arquivo .env.example
- [x] Criar template de variáveis de ambiente:
  ```
  # Database
  DB_HOST=localhost
  DB_PORT=5432
  DB_NAME=msimagens
  DB_USER=
  DB_PASSWORD=
  
  # JWT
  JWT_SECRET=
  JWT_EXPIRATION=1800000
  
  # Cloudflare
  CLOUDFLARE_ACCOUNT_ID=8e858ade7b52abbd9f51c3071f0cbf42
  CLOUDFLARE_GET_TOKEN=uiXHR6nRCvOmsEuJ7Rm0Pv530NS7A_QaYr38KxbN
  CLOUDFLARE_POST_TOKEN=zXnfAoOiLUHdFJhLxjhJW7c9nGxlxX3BduO2IX2Y
  
  # Server
  SERVER_PORT=8080
  ```

### 9.4 - Docker

#### 9.4.1 - Dockerfile
- [x] Criar Dockerfile multi-stage para WAR:
  - Stage 1: Build com Maven (maven:3.9-eclipse-temurin-21)
    - Copiar pom.xml e baixar dependências
    - Copiar código e fazer build (mvn clean package)
  - Stage 2: Runtime com Tomcat
    - Usar tomcat:10-jre21-temurin-alpine
    - Copiar WAR para /usr/local/tomcat/webapps/
    - Configurar variáveis de ambiente
  - Otimizar layers
  - Expor porta 8080
  
#### 9.4.2 - docker-compose.yml
- [x] Criar docker-compose com:
  - Serviço da aplicação (Tomcat + WAR)
    - Build do Dockerfile
    - Porta 8080:8080
    - Depends_on: postgres
  - Serviço do banco de dados (**PostgreSQL 16**)
    - Imagem: postgres:16-alpine
    - Porta 5432:5432
    - Volume persistente
  - Networks (app-network)
  - Volumes (postgres-data)
  - Variables de ambiente (Cloudflare, JWT, Database)
  
- [x] Criar docker-compose.dev.yml (para desenvolvimento)

### 9.5 - Scripts Úteis

- [x] `scripts/start-dev.sh` - Iniciar em modo dev
- [x] `scripts/run-tests.sh` - Executar testes
- [x] `scripts/build.sh` - Build da aplicação
- [x] `scripts/docker-build.sh` - Build da imagem Docker
- [x] `scripts/start-dev.ps1` - Iniciar em modo dev (Windows/PowerShell)

### 9.6 - CI/CD

#### 9.6.1 - GitHub Actions (ou GitLab CI)
- [x] Workflow de CI:
  - Trigger: push, pull_request
  - Jobs:
    - Build
    - Testes unitários
    - Testes de integração
    - Análise de cobertura
    - Build Docker image
    
- [ ] Workflow de CD (opcional) — não implementado nesta task:
  - Deploy automático para ambiente de staging
  - Deploy manual para produção

### 9.7 - Configuração de Profiles

- [x] Profile `dev`:
  - H2 console habilitado
  - Logs detalhados
  - Swagger habilitado
  
- [x] Profile `test`:
  - H2 em memória
  - Dados de teste
  
- [x] Profile `prod`:
  - Logs otimizados
  - Swagger desabilitado (ou protegido)
  - Conexão com banco produção

### 9.8 - Monitoramento e Observabilidade

- [x] Configurar Spring Actuator:
  - Endpoints de health
  - Metrics
  - Info
  
- [ ] Configurar logs estruturados (JSON):
  - Logback configuration
  - Níveis de log por ambiente
  
- [ ] (Opcional) Integrar com:
  - Prometheus (métricas)
  - Grafana (dashboards)
  - ELK Stack (logs centralizados)

### 9.9 - Segurança em Produção

- [ ] Configurar HTTPS/TLS
- [ ] Configurar CORS adequadamente
- [ ] Rate limiting (Spring Cloud Gateway ou Bucket4j)
- [ ] Validar todas as entradas
- [ ] Secrets management (não commitar senhas)
- [ ] Configurar Security Headers:
  - X-Content-Type-Options
  - X-Frame-Options
  - X-XSS-Protection
  - Content-Security-Policy

### 9.10 - Collection do Postman

- [x] Criar collection completa do Postman:
  - Todas as requisições organizadas por recurso
  - Variáveis de ambiente
  - Scripts de autenticação automática
  - Exemplos de payloads
  - Testes automatizados (opcional)
  
- [x] Exportar e versionar no repositório (`postman/ms-imagens.postman_collection.json`)

## Critérios de Aceite
- Documentação OpenAPI completa e funcional
- README detalhado
- Dockerfile e docker-compose funcionando
- CI configurado e passando
- Profiles configurados
- Collection Postman exportada
- Aplicação pronta para deploy
