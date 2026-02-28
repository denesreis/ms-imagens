# Sugestões de Melhoria - Projeto MS-Imagens

**Data da análise:** 18/02/2026  
**Status:** Pendente de revisão

---

## 1. SEGURANÇA - Pontos Críticos

### 1.1 - Tokens Cloudflare expostos na documentação
**Prioridade:** Alta | **Esforço:** Baixo

Os tokens da Cloudflare estão hardcoded em texto plano em vários arquivos:
- `objetivos-do-projeto.md`
- `docs/tasks/complemento-application-yml.md`

**Recomendação:** Nunca colocar secrets reais em arquivos versionados. Usar apenas placeholders.

- [x] Aplicar

---

### 1.2 - JWT sem mecanismo de refresh token
**Prioridade:** Alta | **Esforço:** Médio

O planejamento prevê token de 30 minutos, mas não tem refresh token. O usuário precisa refazer login a cada 30 minutos.

**Sugestão:**
- Adicionar endpoint `POST /api/auth/refresh`
- Refresh token com validade maior (ex: 7 dias)
- Armazenar refresh tokens no banco (permite revogação)

- [x] Aplicar

---

### 1.3 - JWT sem mecanismo de revogação/logout
**Prioridade:** Alta | **Esforço:** Médio

Se um token for comprometido, só expira em 30 minutos. Não existe blacklist de tokens.

**Sugestão:**
- Implementar blacklist em cache (Caffeine ou em memória)
- Endpoint `POST /api/auth/logout` que invalida o token
- Limpar tokens expirados periodicamente

- [x] Aplicar

---

### 1.4 - Sem proteção contra brute force no login
**Prioridade:** Alta | **Esforço:** Baixo

O endpoint de login está público sem proteção contra ataques de força bruta.

**Sugestão:**
- Rate limiting no `POST /api/auth/login` (ex: 5 tentativas em 15 min)
- Bloquear conta temporariamente após N falhas
- Adicionar campos `tentativasLogin` e `bloqueadoAte` na `tb_usuario`

- [x] Aplicar

---

### 1.5 - BCrypt strength baixo
**Prioridade:** Baixa | **Esforço:** Baixo

O planejamento define strength 10 rounds. Para 2026, recomenda-se 12 rounds como mínimo para melhor resistência a ataques.

**Sugestão:**
- Alterar `BCryptPasswordEncoder` de strength 10 para 12

- [x] Aplicar

---

### 1.6 - Sem audit log de ações sensíveis
**Prioridade:** Média | **Esforço:** Médio

Não há previsão de registro de ações sensíveis.

**Sugestão:**
- Criar `tb_audit_log` com campos: acao, usuario, ip, timestamp, detalhes
- Registrar: login/logout, alteração de senha, criação/exclusão de usuários, upload/exclusão de imagens
- Pode usar Spring AOP para interceptar automaticamente

- [x] Aplicar

---

### 1.7 - `spring.security.user` hardcoded
**Prioridade:** Alta | **Esforço:** Baixo

No `complemento-application-yml.md` há:
```yaml
spring.security.user.name: admin
spring.security.user.password: admin
```
Isso não deveria existir num projeto com JWT. Pode criar um backdoor de acesso.

**Sugestão:**
- Remover completamente essa configuração
- A autenticação deve ser 100% via JWT

- [x] Aplicar

---

### 1.8 - Endpoint de criação de usuário público
**Prioridade:** Alta | **Esforço:** Baixo

No `07-camada-apresentacao-controllers.md`, o `POST /api/usuarios` não tem restrição de role. Qualquer pessoa autenticada poderia criar usuários.

**Sugestão:**
- `POST /api/usuarios` deve ser restrito a ADMINISTRADOR
- Ou criar um fluxo separado de auto-registro se necessário

- [x] Aplicar

---

## 2. TECNOLOGIA - Sugestões

### 2.1 - ~~Spring Boot 4.0.2 - Verificar disponibilidade~~ → Resolvido: Spring Boot 3.4.3
**Prioridade:** Alta | **Esforço:** Baixo

~~Precisa confirmar se o Spring Boot 4.0.2 já foi lançado oficialmente.~~ **Resolvido:** Definido Spring Boot **3.4.3** (linha estável 3.4.x, compatível com Java 21).

**Ação aplicada:**
- Versão alterada para **Spring Boot 3.4.3** em todos os arquivos
- Java permanece em **21**
- Compatível com Tomcat 10, Jakarta EE 9+, todas as dependências do projeto

- [x] Aplicar

---

### 2.2 - Cache para consultas frequentes (Caffeine)
**Prioridade:** Média | **Esforço:** Baixo

A consulta de imagens por `codigoEan` tende a ser chamada com alta frequência e pode ser repetitiva.

**Sugestão:**
- Usar **Caffeine** (cache local, simples, sem infraestrutura extra)
- Dependências: `spring-boot-starter-cache` + `com.github.ben-manes.caffeine:caffeine`
- Anotar consultas com `@Cacheable`
- Invalidar cache no create/update/delete de imagens com `@CacheEvict`

- [x] Aplicar

---

### 2.3 - Resilience4j (Circuit Breaker) para Cloudflare
**Prioridade:** Alta | **Esforço:** Médio

Se a API da Cloudflare ficar fora do ar, as requisições vão travar e gerar cascata de falhas.

**Sugestão:**
- Adicionar **Resilience4j** com circuit breaker, retry e timeout
- Dependência: `io.github.resilience4j:resilience4j-spring-boot3`
- Configurar fallback quando Cloudflare estiver indisponível
- Retornar erro amigável ao invés de timeout

- [ ] Aplicar

---

### 2.4 - Processamento assíncrono de upload
**Prioridade:** Média | **Esforço:** Médio

Upload de múltiplas imagens pode ser lento (rede + Cloudflare).

**Sugestão:**
- Usar `@Async` do Spring para uploads em paralelo
- Ou `CompletableFuture` para processar lista de imagens concorrentemente
- Produto é salvo no banco, imagens processadas em background
- Retornar status de processamento (PENDENTE, CONCLUIDO, ERRO)

- [ ] Aplicar

---

### 2.5 - MapStruct ao invés de ModelMapper
**Prioridade:** Baixa | **Esforço:** Baixo

O planejamento deixa em aberto a escolha entre MapStruct e ModelMapper.

**Recomendação: MapStruct**
- Mapeamento em compile-time (não usa reflection)
- Performance muito superior
- Erros detectados em tempo de compilação
- Integração nativa com Lombok

- [x] Aplicar

---

### 2.6 - Versionamento de API
**Prioridade:** Média | **Esforço:** Baixo

Não há estratégia de versionamento dos endpoints. Dificulta evolução futura.

**Sugestão:**
- Usar path versioning: `/api/v1/produtos`, `/api/v1/imagens`
- Facilita evolução futura sem quebrar clientes existentes
- Configurar no `context-path` ou nos controllers

- [x] Aplicar

---

### 2.7 - Paginação padronizada em todos os endpoints de listagem
**Prioridade:** Média | **Esforço:** Baixo

Apenas `ListProdutosUseCase` menciona paginação. Os demais retornam listas completas.

**Sugestão:**
- Padronizar paginação em todos os endpoints de listagem
- Usar `Pageable` do Spring Data em todos os repositórios
- Resposta padronizada: `{ content, page, size, totalElements, totalPages }`
- Endpoints afetados: empresas, usuarios, produtos, imagens

- [x] Aplicar

---

### 2.8 - Compressão de imagem antes do upload
**Prioridade:** Baixa | **Esforço:** Médio

Não há menção de compressão ou redimensionamento de imagens antes do envio à Cloudflare.

**Sugestão:**
- Validar e opcionalmente comprimir imagens antes de enviar
- Biblioteca: **Thumbnailator** (Java, simples e leve)
- Reduz custos de storage e tempo de upload
- Configurar como opcional via propriedade

- [x] Aplicar

---

## 3. ARQUITETURA - Melhorias

### 3.1 - Adicionar campo `status` na `tb_imagem`
**Prioridade:** Média | **Esforço:** Baixo

Para suportar upload assíncrono e rastreabilidade de falhas.

**Sugestão:**
- Adicionar campo `status` (enum: PENDENTE, ATIVO, ERRO)
- `PENDENTE` - aguardando upload na Cloudflare
- `ATIVO` - upload concluído com sucesso
- `ERRO` - falha no upload (permite retry)

- [x] Aplicar

---

### 3.2 - Soft Delete precisa de campo `ativo` nas tabelas originais
**Prioridade:** Alta | **Esforço:** Baixo

O planejamento menciona soft delete, mas as tabelas originais no `objetivos-do-projeto.md` não têm campo `ativo`. Precisa adicionar nas migrations do Flyway.

**Sugestão:**
- Adicionar campo `ativo BOOLEAN DEFAULT TRUE` em todas as tabelas
- Adicionar campos `data_criacao TIMESTAMP` e `data_atualizacao TIMESTAMP`
- Usar `@Where(clause = "ativo = true")` do Hibernate para filtrar automaticamente

- [x] Aplicar

---

### 3.3 - Falta constraint de unicidade no `tb_usuario.nome`
**Prioridade:** Alta | **Esforço:** Baixo

Se `nome` é usado como username para login, precisa ser único. Dois usuários com o mesmo nome causam conflito na autenticação.

**Sugestão:**
- Adicionar unique constraint: `UNIQUE(nome)` na migration
- **Ou** criar campo `email` separado para login (mais profissional e escalável)
- Índice no campo usado para busca de login

- [x] Aplicar

---

### 3.4 - Endpoint de consulta por EAN pode ser público
**Prioridade:** Média | **Esforço:** Baixo

A busca por `codigoEan` retorna apenas imagens abertas (tipo 0). Esse endpoint poderia ser público (sem JWT), facilitando integração com sistemas externos.

**Sugestão:**
- Tornar `GET /api/imagens/ean/{codigoEan}` público no SecurityConfig
- Permite que e-commerces e catálogos consumam as imagens sem autenticação
- As imagens retornadas já são tipo ABERTO, então não há risco de segurança

- [x] Aplicar

---

## 4. RESUMO GERAL

### Tabela de Decisão

| # | Categoria | Sugestão | Impacto | Esforço | Decisão |
|---|-----------|----------|---------|---------|---------|
| 1.1 | Segurança | Remover tokens dos arquivos | **Alto** | Baixo | ✅ |
| 1.2 | Segurança | Refresh Token | **Alto** | Médio | ✅ |
| 1.3 | Segurança | Blacklist/Logout JWT | **Alto** | Médio | ✅ |
| 1.4 | Segurança | Rate limiting no login | **Alto** | Baixo | ✅ |
| 1.5 | Segurança | BCrypt strength 12 | Baixo | Baixo | ✅ |
| 1.6 | Segurança | Audit log | Médio | Médio | ✅ |
| 1.7 | Segurança | Remover spring.security.user | **Alto** | Baixo | ✅ |
| 1.8 | Segurança | POST /usuarios apenas ADMIN | **Alto** | Baixo | ✅ |
| 2.1 | Tecnologia | ~~Spring Boot 4.0.2~~ → 3.4.3 | **Alto** | Baixo | ✅ |
| 2.2 | Tecnologia | Cache (Caffeine) | Médio | Baixo | ✅ |
| 2.3 | Tecnologia | Resilience4j (Circuit Breaker) | **Alto** | Médio | ⬜ |
| 2.4 | Tecnologia | Upload assíncrono | Médio | Médio | ⬜ |
| 2.5 | Tecnologia | MapStruct (definir) | Baixo | Baixo | ✅ |
| 2.6 | Tecnologia | Versionamento de API (v1) | Médio | Baixo | ✅ |
| 2.7 | Tecnologia | Paginação padronizada | Médio | Baixo | ✅ |
| 2.8 | Tecnologia | Compressão de imagem | Baixo | Médio | ✅ |
| 3.1 | Arquitetura | Campo status em tb_imagem | Médio | Baixo | ✅ |
| 3.2 | Arquitetura | Campo ativo nas tabelas | **Alto** | Baixo | ✅ |
| 3.3 | Arquitetura | Unique constraint username | **Alto** | Baixo | ✅ |
| 3.4 | Arquitetura | Endpoint EAN público | Médio | Baixo | ✅ |

**Legenda da Decisão:**
- ⬜ Pendente de análise
- ✅ Aprovado para implementação
- ❌ Rejeitado
- ⏸️ Adiado para versão futura

---

## 5. IMPACTO NAS TASKS

### Se todas as sugestões forem aprovadas:

| Task | Itens Impactados | Estimativa Adicional |
|------|-----------------|---------------------|
| 01 | 2.1, 2.5, 2.6 | +1h |
| 02 | 3.1, 3.2 | +1h |
| 03 | 1.4, 3.2, 3.3 | +2h |
| 04 | 2.3, 2.4, 2.8 | +4h |
| 05 | 1.2, 1.3, 1.4, 1.5, 1.7, 1.8 | +6h |
| 06 | 1.6, 2.2, 2.7, 3.4 | +3h |
| 07 | 1.2, 1.8, 2.6, 2.7, 3.4 | +2h |
| 08 | Testes para novos componentes | +4h |
| 09 | Documentar novos recursos | +1h |
| **TOTAL ADICIONAL** | - | **+24h** |

**Estimativa original:** 74h  
**Estimativa com todas as melhorias:** ~107h

---

**Instruções:** Marque com ✅ as sugestões aprovadas e me avise para que eu atualize as tasks correspondentes.
