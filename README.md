# Verx Task API

API REST para gerenciamento de tarefas construída com **Java 21** e **Spring Boot 3**, com autenticação baseada em JWT, controle de acesso por perfil e suporte completo a Docker.

---

## Sumário

- [Visão Geral](#visão-geral)
- [Tecnologias](#tecnologias)
- [Configuração Local](#configuração-local)
- [Configuração com Docker](#configuração-com-docker)
- [Variáveis de Ambiente](#variáveis-de-ambiente)
- [Referência da API](#referência-da-api)
  - [Autenticação](#autenticação)
  - [Tarefas](#tarefas)
  - [Usuários](#usuários)
- [Exemplos de Uso (curl)](#exemplos-de-uso-curl)
- [Executando os Testes](#executando-os-testes)
- [Análise Estática](#análise-estática)

---

## Visão Geral

A Verx Task API disponibiliza endpoints para:

- **Registrar e autenticar** usuários com access tokens JWT de curta duração e refresh tokens rotativos.
- **Gerenciar tarefas** (criar, listar, atualizar, excluir) com verificação de propriedade por usuário.
- **Controle de acesso por perfil**: `USER` (padrão) acessa apenas suas próprias tarefas; `ADMIN` acessa qualquer tarefa.

O Swagger UI fica disponível em `http://localhost:8080/swagger-ui.html` quando a aplicação estiver em execução.

---

## Tecnologias

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 21 |
| Framework | Spring Boot 3.5 |
| Segurança | Spring Security + JJWT (HS256) |
| Persistência | Spring Data JPA + PostgreSQL 16 |
| Migrations | Liquibase |
| Mapeamento | MapStruct |
| Documentação | springdoc-openapi (Swagger UI) |
| Build | Gradle |
| Containers | Docker + Docker Compose |
| Testes | JUnit 5 + Mockito + Testcontainers |

---

## Configuração Local

### Pré-requisitos

- Java 21
- PostgreSQL 16 rodando localmente (ou use o Docker)
- Gradle (ou use o wrapper `./gradlew`)

### Passos

1. **Clone o repositório**

   ```bash
   git clone <repo-url>
   cd verx-task-api
   ```

2. **Crie o arquivo `.env`** a partir do template:

   ```bash
   cp .env.example .env
   ```

   Edite o `.env` com os seus valores locais (veja [Variáveis de Ambiente](#variáveis-de-ambiente)).

3. **Execute a aplicação** com o perfil `dev`:

   ```bash
   ./gradlew bootRun
   ```

   A API estará disponível em `http://localhost:8080`.

4. **Acesse o Swagger UI**:

   ```
   http://localhost:8080/swagger-ui.html
   ```

---

## Configuração com Docker

Toda a stack (aplicação + PostgreSQL + Redis) pode ser inicializada com um único comando.

### Pré-requisitos

- Docker 24+
- Docker Compose v2

### Passos

1. **Crie o arquivo `.env`** a partir do template:

   ```bash
   cp .env.example .env
   ```

2. **Build e inicialização** de todos os serviços:

   ```bash
   docker compose up --build
   ```

3. **Parar todos os serviços**:

   ```bash
   docker compose down
   ```

   Para remover também os volumes (dados do banco):

   ```bash
   docker compose down -v
   ```

A aplicação estará disponível em `http://localhost:8080`.

---

## Variáveis de Ambiente

Toda a configuração é fornecida por variáveis de ambiente (carregadas do `.env` localmente e injetadas via `docker-compose.yml` nos containers).

| Variável | Descrição | Padrão (dev) |
|---|---|---|
| `DB_HOST` | Hostname do PostgreSQL | `localhost` |
| `DB_PORT` | Porta do PostgreSQL | `5432` |
| `DB_NAME` | Nome do banco de dados | `verx_task_db` |
| `DB_USER` | Usuário do banco de dados | `postgres` |
| `DB_PASSWORD` | Senha do banco de dados | `postgres` |
| `JWT_SECRET` | Segredo HMAC HS256 para assinar tokens (mín. 32 caracteres) | *(padrão apenas para dev)* |
| `JWT_EXP_MINUTES` | Tempo de vida do access token em minutos | `30` |
| `REDIS_HOST` | Hostname do Redis | `localhost` |
| `REDIS_PORT` | Porta do Redis | `6379` |
| `CORS_ALLOWED_ORIGINS` | Origens permitidas pelo CORS, separadas por vírgula | `http://localhost:3000,http://localhost:5173` |

> ⚠️ **Nunca faça commit de segredos reais.** O arquivo `.env` está no `.gitignore`. Use `.env.example` como template.

---

## Referência da API

### URL Base

```
http://localhost:8080
```

### Autenticação

Todos os endpoints de tarefas e usuários exigem um access token JWT válido no cabeçalho `Authorization`:

```
Authorization: Bearer <access_token>
```

---

### Autenticação

| Método | Endpoint | Autenticação | Descrição |
|---|---|---|---|
| `POST` | `/auth/register` | Não | Registrar novo usuário |
| `POST` | `/auth/login` | Não | Login e recebimento dos tokens |
| `POST` | `/auth/refresh` | Não | Renovar o access token |

#### `POST /auth/register`

**Corpo da requisição:**

```json
{
  "name": "João Silva",
  "email": "joao@exemplo.com",
  "password": "senha123"
}
```

**Resposta `201`:**

```json
{
  "accessToken": "<jwt>",
  "refreshToken": "<uuid>",
  "tokenType": "Bearer"
}
```

#### `POST /auth/login`

**Corpo da requisição:**

```json
{
  "email": "joao@exemplo.com",
  "password": "senha123"
}
```

**Resposta `200`:** mesmo formato do registro.

#### `POST /auth/refresh`

**Corpo da requisição:**

```json
{
  "refreshToken": "<uuid>"
}
```

**Resposta `200`:** novo par de access token + refresh token (o refresh token anterior é revogado).

---

### Tarefas

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/tasks` | Criar uma tarefa |
| `GET` | `/tasks` | Listar tarefas (paginado, filtrável) |
| `GET` | `/tasks/{id}` | Buscar tarefa por ID |
| `PUT` | `/tasks/{id}` | Atualizar tarefa |
| `DELETE` | `/tasks/{id}` | Excluir tarefa |

#### Parâmetros de consulta para `GET /tasks`

| Parâmetro | Tipo | Descrição |
|---|---|---|
| `status` | `TODO` \| `IN_PROGRESS` \| `DONE` | Filtrar por status |
| `page` | inteiro | Número da página (0-indexado, padrão `0`) |
| `size` | inteiro | Tamanho da página (padrão `10`) |
| `sort` | string | Campo + direção de ordenação, ex.: `dueDate,asc` |

#### Campos da tarefa

| Campo | Regras |
|---|---|
| `title` | Obrigatório, 3–120 caracteres |
| `description` | Opcional, até 2 000 caracteres |
| `dueDate` | Opcional, não pode ser no passado (`yyyy-MM-dd`) |
| `status` | `TODO` (padrão na criação), `IN_PROGRESS`, `DONE` |

---

### Usuários

| Método | Endpoint | Descrição |
|---|---|---|
| `GET` | `/me` | Obter perfil do usuário autenticado |

---

## Exemplos de Uso (curl)

Substitua `<access_token>` e `<refresh_token>` pelos valores obtidos no login ou registro.

### Registrar

```bash
curl -s -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Alice","email":"alice@exemplo.com","password":"senha1234"}' | jq
```

### Login

```bash
curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"alice@exemplo.com","password":"senha1234"}' | jq
```

### Renovar token

```bash
curl -s -X POST http://localhost:8080/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"<refresh_token>"}' | jq
```

### Obter usuário atual

```bash
curl -s http://localhost:8080/me \
  -H "Authorization: Bearer <access_token>" | jq
```

### Criar uma tarefa

```bash
curl -s -X POST http://localhost:8080/tasks \
  -H "Authorization: Bearer <access_token>" \
  -H "Content-Type: application/json" \
  -d '{"title":"Fazer compras","description":"Leite, ovos, pão","dueDate":"2099-12-31"}' | jq
```

### Listar tarefas (com filtro e paginação)

```bash
curl -s "http://localhost:8080/tasks?status=TODO&page=0&size=5&sort=dueDate,asc" \
  -H "Authorization: Bearer <access_token>" | jq
```

### Buscar tarefa por ID

```bash
curl -s http://localhost:8080/tasks/1 \
  -H "Authorization: Bearer <access_token>" | jq
```

### Atualizar uma tarefa

```bash
curl -s -X PUT http://localhost:8080/tasks/1 \
  -H "Authorization: Bearer <access_token>" \
  -H "Content-Type: application/json" \
  -d '{"title":"Fazer compras","status":"IN_PROGRESS","dueDate":"2099-12-31"}' | jq
```

### Excluir uma tarefa

```bash
curl -s -X DELETE http://localhost:8080/tasks/1 \
  -H "Authorization: Bearer <access_token>"
```

---

## Executando os Testes

### Todos os testes

```bash
./gradlew test
```

### Classe de teste específica

```bash
./gradlew test --tests "br.com.higorcraco.verx_task_api.service.TaskServiceTest"
```

### Relatório de testes

Após a execução, abra o relatório HTML:

```
build/reports/tests/test/index.html
```

> **Obs.:** Os testes de integração e repositório utilizam **Testcontainers** e exigem que o Docker esteja em execução.

---

## Análise Estática

```bash
# Checkstyle
./gradlew checkstyleMain

# SpotBugs
./gradlew spotbugsMain
```

Os relatórios são gerados em `build/reports/checkstyle/` e `build/reports/spotbugs/` respectivamente.
