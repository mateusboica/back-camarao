# Back Camarao

API REST em Spring Boot para autenticacao de usuarios e gerenciamento de cardapio com MongoDB, JWT e documentacao Swagger.

## Visao geral

Este projeto centraliza o backend de um sistema de cardapio digital. A aplicacao oferece:

- cadastro, login e gerenciamento do perfil do usuario autenticado
- autenticacao baseada em JWT
- controle de acesso por perfil (`USER` e `ADMIN`)
- CRUD completo de produtos
- filtros de cardapio por categoria, nome, tag e disponibilidade
- documentacao interativa com Swagger UI

## Stack

- Java 17
- Spring Boot 4
- Spring Web
- Spring Security
- Spring Data MongoDB
- JWT (`jjwt`)
- Swagger / OpenAPI (`springdoc`)
- Maven
- Docker

## Estrutura principal

```text
src/main/java/back/camarao/sistema
|- config        # seguranca, CORS e OpenAPI
|- controller    # endpoints HTTP
|- dto           # contratos de entrada e saida
|- enums         # categorias e roles
|- exception     # tratamento global de erros
|- model         # documentos do MongoDB
|- repository    # acesso a dados
|- security      # filtro JWT e servicos de autenticacao
\- service       # regras de negocio
```

## Requisitos

- Java 17
- Maven 3.9+
- MongoDB acessivel localmente ou em nuvem

## Variaveis de ambiente

O projeto depende destas variaveis:

| Variavel | Obrigatoria | Descricao |
| --- | --- | --- |
| `MONGODB_URI` | Sim | String de conexao do MongoDB |
| `JWT_SECRET` | Sim | Segredo usado para assinar os tokens |
| `JWT_EXPIRATION_MS` | Sim | Tempo de expiracao do token em milissegundos |

Exemplo no PowerShell:

```powershell
$env:MONGODB_URI="mongodb://localhost:27017/back-camarao"
$env:JWT_SECRET="troque-por-um-segredo-forte-com-pelo-menos-32-caracteres"
$env:JWT_EXPIRATION_MS="604800000"
```

## Como rodar localmente

### 1. Subir a aplicacao com Maven

```bash
./mvnw spring-boot:run
```

No Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

### 2. Gerar o pacote

```bash
./mvnw clean package
```

### 3. Executar o JAR

```bash
java -jar target/sistema-0.0.1-SNAPSHOT.jar
```

Por padrao, a API sobe em:

- `http://localhost:8080`

## Docker

Build da imagem:

```bash
docker build -t back-camarao .
```

Execucao do container:

```bash
docker run --rm -p 8080:8080 ^
  -e MONGODB_URI="mongodb://host.docker.internal:27017/back-camarao" ^
  -e JWT_SECRET="troque-por-um-segredo-forte" ^
  -e JWT_EXPIRATION_MS="604800000" ^
  back-camarao
```

## Documentacao da API

Com a aplicacao rodando, acesse:

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Autenticacao

O login retorna:

- um token JWT no corpo da resposta
- um cookie HTTP-only chamado `token`

Observacoes importantes:

- novos usuarios sao cadastrados com perfil `USER`
- rotas de escrita de produtos exigem perfil `ADMIN`
- a API tambem expõe alguns endpoints em versoes curtas, como `/login` e `/register`

## Endpoints principais

### Autenticacao e usuario

| Metodo | Rota | Descricao |
| --- | --- | --- |
| `POST` | `/api/v1/auth/register` | Cadastra usuario |
| `POST` | `/api/v1/auth/login` | Faz login |
| `GET` | `/api/v1/auth/me` | Retorna dados do usuario autenticado |
| `PATCH` | `/api/v1/auth/me` | Atualiza nome e email |
| `PATCH` | `/api/v1/auth/me/senha` | Altera senha |
| `GET` | `/api/v1/auth/logout` | Invalida o cookie de autenticacao |
| `GET` | `/api/v1/auth/me-nome` | Retorna apenas o nome do usuario autenticado |

### Produtos

| Metodo | Rota | Descricao |
| --- | --- | --- |
| `GET` | `/api/v1/produtos` | Lista produtos com paginacao |
| `GET` | `/api/v1/produtos/{id}` | Busca produto por ID |
| `GET` | `/api/v1/produtos/slug/{slug}/` | Busca produto por slug |
| `GET` | `/api/v1/produtos/categoria/{categoria}` | Lista por categoria |
| `GET` | `/api/v1/produtos/busca?termo=...` | Busca por nome |
| `GET` | `/api/v1/produtos/tag/{tag}` | Lista produtos por tag |
| `POST` | `/api/v1/produtos` | Cria produto (`ADMIN`) |
| `PUT` | `/api/v1/produtos/{id}` | Atualiza produto (`ADMIN`) |
| `PATCH` | `/api/v1/produtos/{id}/disponibilidade` | Altera disponibilidade (`ADMIN`) |
| `DELETE` | `/api/v1/produtos/{id}` | Remove produto (`ADMIN`) |

Categorias aceitas:

- `MOQUECAS`
- `FRUTOS_DO_MAR`
- `ENTRADAS`
- `ACOMPANHAMENTOS`
- `BEBIDAS`
- `SOBREMESAS`

## Exemplos de uso

### Cadastro

```http
POST /api/v1/auth/register
Content-Type: application/json

{
  "nome": "Mateus",
  "email": "mateus@email.com",
  "senha": "123456"
}
```

### Login

```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "email": "mateus@email.com",
  "senha": "123456"
}
```

### Criacao de produto

```http
POST /api/v1/produtos
Content-Type: application/json
Authorization: Bearer <token>

{
  "nome": "Camarao na Moranga",
  "preco": 89.90,
  "descricao": "Camarao cremoso servido na moranga.",
  "isDisponivel": true,
  "img": "https://exemplo.com/imagens/camarao-na-moranga.png",
  "categoria": "FRUTOS_DO_MAR",
  "tags": ["camarao", "destaque"]
}
```

### Filtros uteis

```text
GET /api/v1/produtos?disponivel=true
GET /api/v1/produtos/categoria/ENTRADAS?somenteDisponiveis=true
GET /api/v1/produtos/busca?termo=camarao
GET /api/v1/produtos/tag/vegano
```

## Regras de negocio observadas no codigo

- o cadastro cria usuarios sempre com acesso `USER`
- email e nome de usuario precisam ser unicos
- nome do produto precisa ser unico
- o slug do produto e gerado automaticamente a partir do nome
- produtos podem ser ativados ou desativados sem remocao definitiva

## Tratamento de erros

O projeto possui tratamento global para excecoes de validacao, recurso nao encontrado e conflitos de cadastro. Em casos de erro, a API retorna respostas apropriadas para cenarios como:

- `400 Bad Request`
- `401 Unauthorized`
- `403 Forbidden`
- `404 Not Found`
- `409 Conflict`
- `422 Unprocessable Entity`

## Testes

Existem classes de teste no projeto, mas atualmente elas nao trazem cenarios implementados de forma relevante. Vale a pena evoluir essa parte antes de usar o backend em producao.

Para rodar os testes:

```bash
./mvnw test
```