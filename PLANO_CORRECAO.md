# Plano de Correção — Swagger + README + Testes

Análise comparativa entre documentação (README, Swagger/OpenAPI) e código identificou as incompatibilidades abaixo. Este plano descreve as correções a serem aplicadas.

## Incompatibilidades encontradas

### Swagger (`/v3/api-docs`) × Código

| # | Endpoint | Swagger documenta | Código retorna | Gravidade |
|---|---|---|---|---|
| 1 | `POST /api/items` | 200 OK | **201 Created** | Alta |
| 2 | `DELETE /api/items/{id}` | 200 OK | **204 No Content** | Alta |
| 3 | `GET /api/items/{id}` | apenas 200 | também 404 (corpo vazio) | Média |
| 4 | `PUT /api/items/{id}` | apenas 200 | também 400 (validação) e 404 | Média |
| 5 | `POST /api/items` | apenas 200 | também 400 e 415 | Média |
| 6 | `PUT` requestBody | só `application/json` | código não declara `consumes` → aceita XML | Baixa |

Causa raiz: nenhuma anotação OpenAPI (`@Operation`, `@ApiResponse`, `@Schema`) no código — o springdoc gera o spec apenas a partir dos mappings do Spring MVC.

### README × Código

| # | README diz | Realidade | Gravidade |
|---|---|---|---|
| 1 | Documenta 3 endpoints | Existem 5 (faltam `PUT` e `DELETE`) | Alta |
| 2 | Suporte a JSON e XML | Só os `GET`s produzem XML; `POST` produz apenas JSON | Média |
| 3 | Testes validam JSON e XML | Nenhum teste XML existe | Média |
| 4 | "testes de integração" | Não há `@SpringBootTest` | Média |
| 5 | Cenário "JSON Inválido → 400" | Sem teste de JSON malformado | Média |
| 6 | Árvore de diretórios | Indentação quebra em `ItemServiceTest.java` | Baixa |
| 7 | `cd java-rest-api`, typos ("repositório::", "diponível") | Diretório real difere | Baixa |

### Testes × Código

- **15 testes atuais 100% alinhados** com o código (201, 204, 400, 404 batem com o comportamento real).
- Testes conflitam com o Swagger (201/204 vs 200) — os testes estão corretos; o Swagger é quem está desatualizado.

### Lacunas de cobertura

- `POST` com `Content-Type` errado → 415
- `Accept: text/csv` → 406
- `PUT` com body inválido → 400
- `DELETE` id inexistente → 404
- `GET /api/items/abc` → 400
- JSON malformado → 400
- Resposta XML (GET com `Accept: application/xml`)
- Teste de integração ponta a ponta
- `ItemServiceTest`: `deleteItem`/`updateItem` com id inexistente

---

## Fase 1: Swagger alinhado ao código

**`src/main/java/com/example/demo/controller/ApiController.java`** — anotar com springdoc (`io.swagger.v3.oas.annotations.*`):

| Método | Anotações |
|---|---|
| Classe | `@Tag(name = "Items", description = "Gerenciamento de itens")` |
| `getAllItems` | `@Operation` + `@ApiResponse(200, array de Item)` + `@ApiResponse(406)` |
| `createItem` | `@ApiResponse(201)` + `@ApiResponse(400)` + `@ApiResponse(415)` |
| `getItemById` | `@ApiResponse(200)` + `404` + `400` + `406` + `@Parameter` no `id` |
| `updateItem` | `@ApiResponse(200)` + `400` + `404` |
| `deleteItem` | `@ApiResponse(204)` + `400` + `404` |

**`model/Item.java`** — `@Schema` nos campos: `id` com `accessMode = READ_ONLY` (servidor gera), `name`/`description` com descrição, `requiredMode = REQUIRED` e example.

**Novo: `config/OpenApiConfig.java`** — `@OpenAPIDefinition(info = @Info(title = "Java REST API", version = "1.0", ...))` para substituir `{"title": "OpenAPI definition", "version": "v0"}`.

> Corpo do 400 (mapa campo→mensagem) documentado apenas na descrição — sem DTO extra, para não alterar a API.

## Fase 2: README

- Adicionar exemplos de **PUT** e **DELETE** (corpos, headers, códigos retornados).
- Nova seção **"Códigos de status"**: 200/201/204/400/404/406/415 com exemplo de corpo de erro.
- Corrigir árvore de diretórios, `cd java-rest-api` → nome real, typos.
- Especificar que **XML só nos GETs** (`Accept: application/xml`).
- Atualizar seção de testes: listar novos testes (XML, integração, JSON malformado).

## Fase 3: Testes a criar

**`ApiControllerTest.java`** (+7 testes, `@WebMvcTest`/`@MockBean`):
1. POST JSON malformado → 400
2. POST `Content-Type: text/plain` → 415
3. `GET /api/items/1` com `Accept: text/csv` → 406
4. PUT body inválido → 400 (mensagens de validação)
5. DELETE id inexistente → 404
6. `GET /api/items/abc` → 400
7. GET com `Accept: application/xml` → 200 + content-type XML + `xpath("//name")`

**Novo: `ApiIntegrationTest.java`** (`@SpringBootTest(webEnvironment = RANDOM_PORT)` + `TestRestTemplate`):
- Fluxo completo: POST → 201 → GET by id → 200 → PUT → 200 → DELETE → 204 → GET → 404
- POST inválido → 400 (corpo com mensagens); JSON malformado → 400
- `GET /v3/api-docs` → 200 e contendo `"201"`/`"204"` — teste de regressão da documentação
- Sem depender de ids absolutos (cria e limpa os próprios itens)

**`ItemServiceTest.java`** (+2): `deleteItem` id inexistente → `false`; `updateItem` id inexistente → `Optional.empty()`

## Fase 4: Limpeza

- `ApiControllerTest.java`: remover bloco `@Test` comentado (L50-65) e comentário-lixo L155
- `ItemServiceTest.java`: remover L65-67 (`//... (imports e a classe de teste)`)

## Fase 5: Verificação

1. `mvn test` — tudo verde (hoje: 15 → esperado ~25 testes)
2. Reiniciar a app e validar ao vivo:
   - `GET /v3/api-docs` → POST documenta **201**, DELETE **204**, com 400/404/406/415
   - Swagger UI carrega e os 5 endpoints aparecem com exemplos
3. Reexecutar a suíte de smoke via curl (22 checks) para garantir zero regressão

## Arquivos

- **Alterados:** `ApiController.java`, `Item.java`, `README.md`, `ApiControllerTest.java`, `ItemServiceTest.java`
- **Novos:** `config/OpenApiConfig.java`, `ApiIntegrationTest.java`
- **Sem mudança de comportamento da API.**
