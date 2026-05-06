# Task 10 - Renomear Projeto: ms-imagens → ms-bluedot

## Objetivo
Renomear o projeto de `ms-imagens` para `ms-bluedot`, pois o escopo do serviço será expandido além do gerenciamento de imagens.

## Contexto
O projeto foi inicialmente criado como um micro-serviço focado em gerenciamento de imagens. Com a adição de novas funcionalidades no roadmap, o nome `ms-bluedot` representa melhor a identidade do serviço sem limitar seu escopo.

---

## Subtasks

### 10.1 - Renomear a pasta raiz do projeto
- [ ] Renomear `c:\WS-SCA\ms-imagens\` → `c:\WS-SCA\ms-bluedot\`
- [ ] Atualizar o workspace no VS Code apontando para a nova pasta

> ⚠️ **Pendente:** Fechar o VS Code, executar `Rename-Item "c:\WS-SCA\ms-imagens" "ms-bluedot"` no PowerShell e reabrir o workspace em `c:\WS-SCA\ms-bluedot`.

---

### 10.2 - Renomear arquivos físicos

| Arquivo atual | Novo nome |
|---|---|
| `postman/ms-imagens.postman_collection.json` | `postman/ms-bluedot.postman_collection.json` |
| `src/main/java/com/scasistemas/msimagens/MsImagensApplication.java` | `MsBluedotApplication.java` |
| `src/test/java/com/scasistemas/msimagens/MsImagensApplicationTests.java` | `MsBluedotApplicationTests.java` |

---

### 10.3 - Renomear pacotes Java (diretórios)

Renomear os diretórios de pacote de `msimagens` para `msbluedot`:

```
src/main/java/com/scasistemas/msimagens/  →  src/main/java/com/scasistemas/msbluedot/
src/test/java/com/scasistemas/msimagens/  →  src/test/java/com/scasistemas/msbluedot/
```

> **Observação:** Esta operação é melhor realizada via IDE (Rename Package no IntelliJ/VS Code com suporte Java) para garantir que todos os `package` e `import` sejam atualizados automaticamente nos ~176 arquivos `.java`.

---

### 10.4 - Atualizar `.github/workflows/ci.yml`

Arquivo: `.github/workflows/ci.yml`

| Linha | Valor atual | Novo valor |
|---|---|---|
| ~23 | `POSTGRES_DB: msimagens_test` | `POSTGRES_DB: msbluedot_test` |
| ~92 | `name: ms-imagens-war` | `name: ms-bluedot-war` |
| ~117 | `tags: ms-imagens:${{ github.sha }},ms-imagens:latest` | `tags: ms-bluedot:${{ github.sha }},ms-bluedot:latest` |

---

### 10.5 - Atualizar `pom.xml`

Arquivo: `pom.xml`

| Campo | Valor atual | Novo valor |
|---|---|---|
| `<artifactId>` | `ms-imagens` | `ms-bluedot` |
| `<name>` | `ms-imagens` | `ms-bluedot` |
| `<description>` | `Micro-serviço de gerenciamento de imagens integrado com Cloudflare Images API` | `Micro-serviço BlueDot integrado com Cloudflare Images API` |
| DB URL padrão (linha ~311) | `jdbc:postgresql://localhost:5432/msimagens` | `jdbc:postgresql://localhost:5432/msbluedot` |

---

### 10.6 - Atualizar arquivos de configuração `application.yml`

#### `src/main/resources/application.yml`
| Linha | Valor atual | Novo valor |
|---|---|---|
| ~3 | `name: ms-imagens` | `name: ms-bluedot` |
| ~106 | `com.scasistemas.msimagens: INFO` | `com.scasistemas.msbluedot: INFO` |

#### `src/main/resources/application-dev.yml`
| Linha | Valor atual | Novo valor |
|---|---|---|
| ~4 | `${DB_NAME:msimagens}` (na JDBC URL) | `${DB_NAME:msbluedot}` |
| ~35 | `com.scasistemas.msimagens: DEBUG` | `com.scasistemas.msbluedot: DEBUG` |

#### `src/main/resources/application-prod.yml`
| Linha | Valor atual | Novo valor |
|---|---|---|
| ~4 | `${DATABASE_URL:jdbc:postgresql://localhost:5432/msimagens}` | `${DATABASE_URL:jdbc:postgresql://localhost:5432/msbluedot}` |
| ~39 | `com.scasistemas.msimagens: INFO` | `com.scasistemas.msbluedot: INFO` |
| ~42 | `name: /var/log/ms-imagens/application.log` | `name: /var/log/ms-bluedot/application.log` |

#### `src/main/resources/application-test.yml`
| Linha | Valor atual | Novo valor |
|---|---|---|
| ~38 | `com.scasistemas.msimagens: INFO` | `com.scasistemas.msbluedot: INFO` |

---

### 10.7 - Atualizar arquivos de classe Java

#### `MsBluedotApplication.java` (renomeado de `MsImagensApplication.java`)
```java
// Antes:
package com.scasistemas.msimagens;
// ...
* Classe principal do micro-serviço ms-imagens.
public class MsImagensApplication {

// Depois:
package com.scasistemas.msbluedot;
// ...
* Classe principal do micro-serviço ms-bluedot.
public class MsBluedotApplication {
```

> **Atenção:** O Javadoc na linha ~9 também contém `ms-imagens` e precisa ser atualizado.

#### `ServletInitializer.java`
```java
// Antes:
package com.scasistemas.msimagens;
// ...
configure(MsImagensApplication.class);

// Depois:
package com.scasistemas.msbluedot;
// ...
configure(MsBluedotApplication.class);
```

#### `MsBluedotApplicationTests.java` (renomeado de `MsImagensApplicationTests.java`)
```java
// Antes:
package com.scasistemas.msimagens;
public class MsImagensApplicationTests {

// Depois:
package com.scasistemas.msbluedot;
public class MsBluedotApplicationTests {
```

#### Todos os outros arquivos `.java` (main + test)
- Substituição global: `com.scasistemas.msimagens` → `com.scasistemas.msbluedot`
- Afeta declarações `package` e `import` em aproximadamente **176 arquivos**

---

### 10.8 - Atualizar `Dockerfile`

Arquivo: `Dockerfile`

| Linha | Valor atual | Novo valor |
|---|---|---|
| ~25 | `COPY --from=build /app/target/ms-imagens.war /usr/local/tomcat/webapps/ROOT.war` | `COPY --from=build /app/target/ms-bluedot.war /usr/local/tomcat/webapps/ROOT.war` |
| ~28 | `RUN mkdir -p /var/log/ms-imagens` | `RUN mkdir -p /var/log/ms-bluedot` |

---

### 10.9 - Atualizar `docker-compose.yml`

Arquivo: `docker-compose.yml`

| Linha | Valor atual | Novo valor |
|---|---|---|
| ~4 | `# MS Imagens - Docker Compose (Produção/Staging)` | `# MS BlueDot - Docker Compose (Produção/Staging)` |
| ~10 | `# Aplicação - MS Imagens (Tomcat + WAR)` | `# Aplicação - MS BlueDot (Tomcat + WAR)` |
| ~16 | `container_name: ms-imagens-app` | `container_name: ms-bluedot-app` |
| ~25 | `DATABASE_URL: jdbc:postgresql://postgres:5432/${DB_NAME:-msimagens}` | `DATABASE_URL: jdbc:postgresql://postgres:5432/${DB_NAME:-msbluedot}` |
| ~51 | `- app-logs:/var/log/ms-imagens` | `- app-logs:/var/log/ms-bluedot` |
| ~64 | `container_name: ms-imagens-postgres` | `container_name: ms-bluedot-postgres` |
| ~67 | `POSTGRES_DB: ${DB_NAME:-msimagens}` | `POSTGRES_DB: ${DB_NAME:-msbluedot}` |
| ~78 | `pg_isready -U ${DB_USER} -d ${DB_NAME:-msimagens}` | `pg_isready -U ${DB_USER} -d ${DB_NAME:-msbluedot}` |

---

### 10.10 - Atualizar `docker-compose.dev.yml`

Arquivo: `docker-compose.dev.yml`

| Linha | Valor atual | Novo valor |
|---|---|---|
| ~4 | `# MS Imagens - Docker Compose para DESENVOLVIMENTO` | `# MS BlueDot - Docker Compose para DESENVOLVIMENTO` |
| ~15 | `container_name: ms-imagens-postgres-dev` | `container_name: ms-bluedot-postgres-dev` |
| ~19 | `POSTGRES_DB: msimagens` | `POSTGRES_DB: msbluedot` |
| ~28 | `container_name: ms-imagens-pgadmin` | `container_name: ms-bluedot-pgadmin` |

---

### 10.11 - Atualizar scripts

#### `scripts/docker-build.sh`
| Linha | Valor atual | Novo valor |
|---|---|---|
| ~9 | `IMAGE_NAME="${IMAGE_NAME:-ms-imagens}"` | `IMAGE_NAME="${IMAGE_NAME:-ms-bluedot}"` |

#### `scripts/start-dev.sh`
| Linha | Valor atual | Novo valor |
|---|---|---|
| ~17 | `docker exec ms-imagens-postgres-dev pg_isready ...` | `docker exec ms-bluedot-postgres-dev pg_isready ...` |

#### `scripts/start-dev.ps1`
| Linha | Valor atual | Novo valor |
|---|---|---|
| ~25 | `docker exec ms-imagens-postgres-dev pg_isready ...` | `docker exec ms-bluedot-postgres-dev pg_isready ...` |

---

### 10.12 - Atualizar coleção Postman

Arquivo: `postman/ms-bluedot.postman_collection.json` (já renomeado na task 10.2)

| Campo | Valor atual | Novo valor |
|---|---|---|
| `_postman_id` | `ms-imagens-api-collection` | `ms-bluedot-api-collection` |
| `name` | `MS Imagens API` | `MS BlueDot API` |

---

### 10.13 - Atualizar `README.md`

Arquivo: `README.md`

Substituições necessárias:
- `ms-imagens` → `ms-bluedot` (todas as ocorrências)
- `msimagens` → `msbluedot` (todas as ocorrências)
- `com/scasistemas/msimagens/` → `com/scasistemas/msbluedot/`
- `CREATE DATABASE msimagens;` → `CREATE DATABASE msbluedot;`
- `target/ms-imagens.war` → `target/ms-bluedot.war`
- Atualizar título e descrição para refletir o novo escopo do projeto

---

### 10.14 - Atualizar Flyway Migrations (comentários)

| Arquivo | Linha | Valor atual | Novo valor |
|---|---|---|---|
| `src/main/resources/db/migration/V1__create_tables.sql` | ~3 | `-- ms-imagens - Flyway Migration` | `-- ms-bluedot - Flyway Migration` |
| `src/main/resources/db/migration/V2__insert_initial_data.sql` | ~3 | `-- ms-imagens - Flyway Migration` | `-- ms-bluedot - Flyway Migration` |

> **Nota:** Apenas comentários SQL são afetados. Nenhuma alteração estrutural nas migrations.

---

### 10.15 - Atualizar `.env.example`

Arquivo: `.env.example`

| Linha | Valor atual | Novo valor |
|---|---|---|
| ~1 | `# Variáveis de Ambiente - ms-imagens` | `# Variáveis de Ambiente - ms-bluedot` |
| ~11 | `DB_NAME=msimagens` | `DB_NAME=msbluedot` |
| ~14 | `DATABASE_URL=jdbc:postgresql://localhost:5432/msimagens` | `DATABASE_URL=jdbc:postgresql://localhost:5432/msbluedot` |

---

### 10.16 - Atualizar documentação em `docs/`

#### `docs/sugestoes-de-melhoria.md`
- `MS-Imagens` → `MS-BlueDot`

#### `docs/tasks/00-resumo-tasks.md`
- `# Resumo do Plano de Tasks - MS Imagens` → `# Resumo do Plano de Tasks - MS BlueDot`

#### `docs/tasks/09-documentacao-deploy.md`
- `msimagens` → `msbluedot`
- `ms-imagens.postman_collection.json` → `ms-bluedot.postman_collection.json`

#### `docs/tasks/complemento-pom-xml.md`
- `msimagens` → `msbluedot`
- `com.empresa.msimagens` → `com.empresa.msbluedot`
- `MsImagensApplication` → `MsBluedotApplication`

#### `docs/tasks/complemento-application-yml.md`
- `msimagens` → `msbluedot`
- `ms-imagens` → `ms-bluedot`
- `com.empresa.msimagens` → `com.empresa.msbluedot`

#### `docs/tasks/01-setup-inicial.md`
- `com/empresa/msimagens/` → `com/empresa/msbluedot/`

---

### 10.17 - Atualizar banco de dados (se já existente)

Caso o banco de dados já tenha sido criado com o nome anterior:

```sql
-- Renomear o banco de dados (requer conexão em outro banco, ex: postgres)
ALTER DATABASE msimagens RENAME TO msbluedot;
```

> **Atenção:** Verificar se há conexões ativas antes de executar. Atualizar também as variáveis de ambiente `.env` com `DB_NAME=msbluedot`.

---

### 10.18 - Atualizar variáveis de ambiente

Arquivo: `.env`

| Variável | Valor atual | Novo valor |
|---|---|---|
| `DB_NAME` | `msimagens` | `msbluedot` |

---

### 10.19 - Limpar arquivos gerados

Arquivos de output gerados que contêm referências ao nome antigo:

- [x] Deletar `test-output.txt` (será regenerado pelo `mvn test`)
- [x] Deletar `mvn-check.txt` (será regenerado)
- [x] Deletar pasta `target/` com `mvn clean`
- [ ] (Opcional) Arquivo `.github/java-upgrade/.../0.log` contém paths antigos — pode ser mantido como histórico

> **Nota:** Estes são arquivos gerados automaticamente. Não precisam ser editados manualmente — basta regenerá-los após a renomeação.

---

### 10.20 - Verificar e compilar o projeto

- [ ] Executar `mvn clean compile` e verificar se não há erros de compilação
- [ ] Executar `mvn test` para verificar se os testes passam
- [ ] Verificar se o WAR gerado se chama `ms-bluedot.war`
- [ ] Executar busca global por `msimagens` e `ms-imagens` para garantir que nenhuma referência foi esquecida

---

## Ordem de Execução Recomendada

1. **10.1** — Renomear pasta raiz (fazer fora do VS Code para evitar conflitos)
2. **10.3** — Renomear pacotes Java via IDE (rename package automático)
3. **10.2** — Renomear arquivos físicos (classes Java + Postman)
4. **10.4 a 10.16** — Atualizar conteúdo dos arquivos (pode ser feito em qualquer ordem)
5. **10.17** — Renomear banco de dados (se aplicável)
6. **10.18** — Atualizar variáveis de ambiente
7. **10.19** — Limpar arquivos gerados (test-output.txt, mvn-check.txt, target/)
8. **10.20** — Validar compilação, testes e busca global por referências remanescentes

---

## Critérios de Aceite

- [x] Pasta raiz renomeada para `ms-bluedot`
- [x] Pacote Java alterado de `com.scasistemas.msimagens` para `com.scasistemas.msbluedot`
- [x] `pom.xml` com `artifactId` e `name` = `ms-bluedot`
- [x] Todos os arquivos de configuração YAML atualizados
- [x] CI/CD workflow (`.github/workflows/ci.yml`) atualizado
- [x] Dockerfile e docker-compose atualizados (incluindo comentários)
- [x] Scripts atualizados
- [x] Flyway migrations (comentários SQL) atualizados
- [x] `.env.example` atualizado
- [x] Coleção Postman renomeada e atualizada
- [x] README.md atualizado
- [x] Documentação em `docs/` atualizada (incluindo `00-resumo-tasks.md`)
- [x] Arquivos gerados limpos (`test-output.txt`, `mvn-check.txt`, `target/`)
- [x] Projeto compila sem erros (`mvn clean compile`)
- [x] Testes passam (`mvn test`)
- [x] WAR gerado com nome `ms-bluedot.war`
- [x] Busca global por `msimagens` e `ms-imagens` retorna zero resultados em código-fonte
