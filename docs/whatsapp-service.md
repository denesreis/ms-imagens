# Planejamento: Serviço de Envio de Mensagens via WhatsApp

## Contexto

Novo serviço a ser adicionado ao `ms-bluedot` para permitir o envio de mensagens via WhatsApp com suporte a arquivos anexos (PDF ou XML) e texto livre. Segue a mesma arquitetura limpa (Clean Architecture) já adotada no projeto.

---

## Decisão de Provider

### Opções Avaliadas

| Provider       | Modelo       | Custo        | Integração | Observações                           |
|----------------|--------------|--------------|------------|---------------------------------------|
| Evolution API  | Self-hosted  | Gratuito     | REST API   | Popular no Brasil, open source        |
| Z-API          | SaaS         | Pago por uso | REST API   | Simples, exige conta e plano          |
| Twilio         | SaaS         | Pago por uso | SDK/REST   | Confiável, mas caro e burocrático     |
| Meta Cloud API | SaaS oficial | Pago por uso | REST API   | Requer verificação de empresa no Meta |

### Escolha: Evolution API (recomendado)

**Motivos:**
- Gratuito e open-source (auto-hospedado)
- API REST simples com suporte a texto, documentos PDF e arquivos
- Amplamente adotado em projetos brasileiros
- Permite múltiplas instâncias WhatsApp
- Sem dependência de conta em serviço externo pago

**Alternativa:** Z-API pode ser plugado no lugar com mínimo ajuste, pois o design será baseado em interface.

---

## Endpoint

### `POST /api/v1/whatsapp/enviar`

**Auth:** Bearer JWT (role: USUARIO ou ADMINISTRADOR)

**Content-Type:** `multipart/form-data`

#### Campos

| Campo      | Tipo          | Obrigatório | Descrição                                                       |
|------------|---------------|-------------|------------------------------------------------------------------|
| `telefone` | `String`      | Sim         | Número com DDI+DDD (ex: `5511999999999`)                        |
| `mensagem` | `String`      | Sim         | Texto da mensagem (máx. 4096 caracteres)                        |
| `arquivo`  | `MultipartFile` | Não       | Arquivo PDF ou XML a ser enviado como documento                 |

**Tipos de arquivo aceitos:**
- `application/pdf` — enviado como documento PDF
- `application/xml` / `text/xml` — enviado como documento XML
- Tamanho máximo configurável via `WHATSAPP_MAX_FILE_SIZE_MB` (default: 10 MB)

#### Exemplo de Request (curl)

```bash
curl -X POST https://mercador.bluedoterp.com.br/api/v1/whatsapp/enviar \
  -H "Authorization: Bearer {token}" \
  -F "telefone=5511999999999" \
  -F "mensagem=Segue em anexo seu documento." \
  -F "arquivo=@nota_fiscal.pdf"
```

#### Response 200 OK

```json
{
  "status": "ENVIADO",
  "telefone": "5511999999999",
  "mensagemId": "BAE5DB4FAEDC7E54",
  "timestamp": "2026-06-15T14:30:00Z"
}
```

#### Response 422 Unprocessable Entity (erro de negócio)

```json
{
  "status": "ERRO",
  "codigo": "WHATSAPP_SEND_FAILED",
  "mensagem": "Falha ao enviar mensagem: número inválido."
}
```

---

## Arquitetura (Clean Architecture)

### Diagrama de Camadas

```
[WhatsappController]
       ↓
[EnviarMensagemWhatsappUseCase]
       ↓
[IWhatsappGateway]  ←→  [EvolutionApiClient]
```

---

### 1. Domain Layer

**Entidade de domínio** (sem persistência — mensagens são stateless):
```
domain/
└── whatsapp/
    ├── MensagemWhatsapp.java        # Objeto de domínio (telefone, mensagem, arquivo)
    ├── ResultadoEnvio.java          # Resultado retornado pelo gateway
    ├── enums/
    │   └── StatusEnvioEnum.java     # ENVIADO, ERRO
    ├── exceptions/
    │   └── WhatsappException.java   # Exceção de domínio para falhas de envio
    └── services/
        └── IWhatsappGateway.java    # Interface do gateway (implementado na infra)
```

**`MensagemWhatsapp`** — objeto de valor com validação:
- `telefone`: não nulo, somente dígitos, 10–13 caracteres
- `mensagem`: não nulo, máx. 4096 chars
- `arquivo`: opcional, `byte[]` + `nomeArquivo` + `mimeType`

**`IWhatsappGateway`:**
```java
public interface IWhatsappGateway {
    ResultadoEnvio enviarTexto(MensagemWhatsapp mensagem);
    ResultadoEnvio enviarComArquivo(MensagemWhatsapp mensagem);
}
```

---

### 2. Application Layer

```
application/
├── usecases/
│   └── EnviarMensagemWhatsappUseCase.java
└── dto/
    ├── EnviarMensagemWhatsappRequestDto.java
    └── EnviarMensagemWhatsappResponseDto.java
```

**`EnviarMensagemWhatsappUseCase`** — orquestra:
1. Valida e constrói `MensagemWhatsapp` a partir do DTO
2. Se `arquivo` presente → chama `gateway.enviarComArquivo()`
3. Caso contrário → chama `gateway.enviarTexto()`
4. Retorna `EnviarMensagemWhatsappResponseDto`

---

### 3. Infrastructure Layer

```
infrastructure/
├── web/controllers/
│   └── WhatsappController.java
└── whatsapp/
    ├── EvolutionApiClient.java       # Implementação de IWhatsappGateway
    ├── EvolutionApiProperties.java   # @ConfigurationProperties
    └── dto/
        ├── EvolutionSendTextRequest.java
        ├── EvolutionSendMediaRequest.java
        └── EvolutionApiResponse.java
```

**`EvolutionApiClient`** — usa `WebClient` (já configurado no projeto):
- `POST {WHATSAPP_API_URL}/message/sendText/{instance}` — texto simples
- `POST {WHATSAPP_API_URL}/message/sendMedia/{instance}` — com arquivo (base64)
- Header: `apikey: {WHATSAPP_API_KEY}`
- Arquivo é convertido para Base64 antes do envio

---

### 4. Config Layer

```
config/
└── WhatsappConfig.java   # Bean do WebClient para Evolution API
```

---

## Variáveis de Ambiente (novas)

| Variável                     | Obrigatório | Default   | Descrição                                              |
|------------------------------|-------------|-----------|--------------------------------------------------------|
| `WHATSAPP_API_URL`           | Sim         | —         | URL base da Evolution API (ex: `http://evolution:8080`) |
| `WHATSAPP_API_KEY`           | Sim         | —         | Chave de autenticação da Evolution API                 |
| `WHATSAPP_INSTANCE`          | Sim         | —         | Nome da instância WhatsApp na Evolution API            |
| `WHATSAPP_MAX_FILE_SIZE_MB`  | Não         | `10`      | Tamanho máximo do arquivo em MB                        |

---

## Fluxo de Envio com Arquivo

```
Cliente → POST /api/v1/whatsapp/enviar (multipart)
         ↓
WhatsappController.enviarMensagem()
         ↓
EnviarMensagemWhatsappUseCase.execute()
  ├── Valida telefone (somente dígitos, 10-13 chars)
  ├── Valida mensagem (não vazio, máx. 4096 chars)
  ├── Valida arquivo (MIME: pdf | xml, tamanho ≤ MAX)
  ├── Constrói MensagemWhatsapp
  └── Chama IWhatsappGateway
         ↓
EvolutionApiClient.enviarComArquivo()
  ├── Converte arquivo para Base64
  ├── Detecta mediaType: "document"
  ├── Monta payload Evolution API
  └── POST /message/sendMedia/{instance}
         ↓
Evolution API → WhatsApp
         ↓
ResultadoEnvio → EnviarMensagemWhatsappResponseDto → 200 OK
```

---

## Tratamento de Erros

| Cenário                           | HTTP | Código de Erro              |
|-----------------------------------|------|-----------------------------|
| Telefone inválido                 | 400  | `TELEFONE_INVALIDO`         |
| Mensagem vazia                    | 400  | `MENSAGEM_VAZIA`            |
| Tipo de arquivo não suportado     | 400  | `TIPO_ARQUIVO_NAO_SUPORTADO`|
| Arquivo excede tamanho máximo     | 400  | `ARQUIVO_MUITO_GRANDE`      |
| Evolution API indisponível        | 502  | `WHATSAPP_UNAVAILABLE`      |
| Falha no envio (resposta de erro) | 422  | `WHATSAPP_SEND_FAILED`      |

Todos os erros seguem o handler global já existente no projeto (`GlobalExceptionHandler`).

---

## Segurança

- Endpoint protegido por JWT (mesma configuração do projeto)
- Arquivo recebido é processado em memória (não salvo em disco)
- Conversão para Base64 ocorre no servidor antes de chamar a Evolution API
- Telefone sanitizado: remove caracteres não-numéricos antes da validação
- Rate limiting: considerar adicionar um `@RateLimiter` via Resilience4j para evitar abuso

---

## Testes

### Unitários
- `EnviarMensagemWhatsappUseCaseTest` — mock do `IWhatsappGateway`
  - Cenário: envio apenas com texto
  - Cenário: envio com arquivo PDF
  - Cenário: envio com arquivo XML
  - Cenário: telefone inválido → exceção
  - Cenário: arquivo com tipo não suportado → exceção

### Integração
- `WhatsappControllerTest` — `@SpringBootTest` + mock do `IWhatsappGateway`
  - Testa serialização do request multipart
  - Testa respostas de erro HTTP

> **Nota:** A integração real com a Evolution API **não deve ser testada** em testes automatizados — usar mock/stub do gateway.

---

## Dependências Maven (novas)

Nenhuma dependência nova necessária. O projeto já possui:
- `spring-boot-starter-webflux` — `WebClient` para chamadas HTTP
- `spring-boot-starter-web` — suporte a `MultipartFile`

---

## Ordem de Implementação

1. **Domain** — `MensagemWhatsapp`, `ResultadoEnvio`, `StatusEnvioEnum`, `WhatsappException`, `IWhatsappGateway`
2. **Application** — DTOs + `EnviarMensagemWhatsappUseCase`
3. **Infrastructure** — `EvolutionApiProperties`, `WhatsappConfig`, `EvolutionApiClient`
4. **Controller** — `WhatsappController`
5. **Security** — liberar `/api/v1/whatsapp/**` para autenticação JWT no `SecurityConfig`
6. **Env vars** — adicionar ao `.env.example`
7. **Testes** — unitários e integração
8. **Docs** — atualizar Swagger/OpenAPI description

---

## Estimativa de Esforço

| Etapa                          | Estimativa |
|--------------------------------|------------|
| Domain + Application           | ~2h        |
| Infrastructure (Evolution API) | ~3h        |
| Controller + Security          | ~1h        |
| Testes                         | ~2h        |
| **Total**                      | **~8h**    |
