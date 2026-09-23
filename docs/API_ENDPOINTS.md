# 📡 Catálogo de Endpoints e Rotas - BeautySalon

Este documento cataloga integralmente as **APIs RESTful (OpenAPI / Swagger)** e as **Rotas Web (MVC/Thymeleaf)** do sistema **BeautySalon** (`Proj_Studio`), detalhando parâmetros, cabeçalhos, payloads JSON, permissões de acesso e exemplos práticos de consumo.

---

## 📌 Sumário

1. [Visão Geral e Swagger UI](#1-visão-geral-e-swagger-ui)
2. [Documentação OpenAPI / Swagger](#2-documentação-openapi--swagger)
3. [Endpoints da API RESTful v1 (`/api/v1/{slug}/...`)](#3-endpoints-da-api-restful-v1-apiv1slug)
   * [3.1. API de Clientes (`/api/v1/{slug}/clientes`)](#31-api-de-clientes-apiv1slugclientes)
   * [3.2. API de Agendamentos (`/api/v1/{slug}/agendamentos`)](#32-api-de-agendamentos-apiv1slugagendamentos)
   * [3.3. API de Estoque & Produtos (`/api/v1/{slug}/estoque`)](#33-api-de-estoque--produtos-apiv1slugestoque)
   * [3.4. API de Financeiro & Comandas (`/api/v1/{slug}/financeiro`)](#34-api-de-financeiro--comandas-apiv1slugfinanceiro)
4. [Catálogo de Rotas Web (Thymeleaf MVC)](#4-catálogo-de-rotas-web-thymeleaf-mvc)
5. [Tratamento Global de Erros REST (ProblemDetails)](#5-tratamento-global-de-erros-rest-problemdetails)

---

## 1. Visão Geral e Swagger UI

* **URL Base Padrão**: `http://localhost:8081`
* **Swagger UI Interativo**: `http://localhost:8081/swagger-ui/index.html`
* **Especificação OpenAPI JSON**: `http://localhost:8081/v3/api-docs`
* **Formato de Dados REST**: `application/json` (UTF-8)
* **Tratamento de CSRF**:
  * Rotas sob `/api/**` e rotas do Swagger `/v3/api-docs/**`, `/swagger-ui/**` estão configuradas no `SecurityConfig` para ignorar a obrigatoriedade do token CSRF.
* **Autenticação nas APIs**:
  * As rotas `/api/**` estão abertas para integração e consumo por front-ends/apps.

---

## 2. Documentação OpenAPI / Swagger

O projeto utiliza **Springdoc OpenAPI 3 (v2.8.5)** compatível com Spring Boot 3.5.x.
Acesse pelo navegador:
```
http://localhost:8081/swagger-ui/index.html
```

---

## 3. Endpoints da API RESTful v1 (`/api/v1/{slug}/...`)

### 3.1. API de Clientes (`/api/v1/{slug}/clientes`)

Gerenciada por [ClienteRestController](file:///c:/Dev/Projetos/Proj_Studio/src/main/java/com/beautysalon/API/ClienteRestController.java).

#### A. Listar Todos os Clientes
* **Método**: `GET`
* **URL**: `/api/clientes`
* **Resposta Sucesso (200 OK)**:
```json
[
  {
    "id": 1,
    "nome": "Keilla Silva",
    "email": "keilla@exemplo.com",
    "telefone": "11988887777",
    "dataNascimento": null
  },
  {
    "id": 2,
    "nome": "Carlos Mendes",
    "email": "carlos@exemplo.com",
    "telefone": "11977776666",
    "dataNascimento": null
  }
]
```

#### B. Buscar Cliente por ID
* **Método**: `GET`
* **URL**: `/api/clientes/{id}`
* **Parâmetros de Path**:
  * `id` (`Long`): Identificador único do cliente.
* **Exemplo cURL**:
```bash
curl -X GET http://localhost:8081/api/clientes/1
```

> [!WARNING]
> Consulte o documento `ANALISE_TECNICA_E_MELHORIAS.md`. O método atual possui uma condição invertida de retorno que deve ser corrigida para evitar retorno de 404 em itens válidos.

#### C. Criar Novo Cliente
* **Método**: `POST`
* **URL**: `/api/clientes`
* **Cabeçalhos**: `Content-Type: application/json`
* **Corpo da Requisição (Body)**:
```json
{
  "nome": "Mariana Ferreira",
  "email": "mariana@gmail.com",
  "telefone": "11965432100"
}
```
* **Resposta Sucesso (200 OK)**:
```json
{
  "id": 3,
  "nome": "Mariana Ferreira",
  "email": "mariana@gmail.com",
  "telefone": "11965432100",
  "dataNascimento": null
}
```

#### D. Atualizar Cliente Existente
* **Método**: `PUT`
* **URL**: `/api/clientes/{id}`
* **Parâmetros de Path**: `id` (`Long`)
* **Corpo da Requisição (Body)**:
```json
{
  "nome": "Mariana Ferreira Lima",
  "email": "mariana.lima@gmail.com",
  "telefone": "11965432100"
}
```
* **Resposta Sucesso (200 OK)**: Dados do cliente atualizados.

#### E. Deletar Cliente
* **Método**: `DELETE`
* **URL**: `/api/clientes/{id}`
* **Resposta Sucesso (204 No Content)**.

---

### 2.2. API de Serviços (`/api/servicos`)

Gerenciada por [ServicoRestController](file:///c:/Dev/Projetos/Proj_Studio/src/main/java/com/beautysalon/API/ServicoRestController.java).

#### A. Listar Todos os Serviços do Catálogo
* **Método**: `GET`
* **URL**: `/api/servicos`
* **Resposta Sucesso (200 OK)**:
```json
[
  {
    "id": 1,
    "nome": "Corte de Cabelo Feminino",
    "descricao": "Lavagem, corte e finalização com escova.",
    "preco": 120.00,
    "imagem": "corte_feminino.jpg"
  },
  {
    "id": 2,
    "nome": "Manicure e Pedicure",
    "descricao": "Tratamento completo de unhas das mãos e pés.",
    "preco": 75.00,
    "imagem": "manicure.jpg"
  }
]
```

---

### 2.3. API de SMS (`/api/sms`)

Gerenciada por [SmsController](file:///c:/Dev/Projetos/Proj_Studio/src/main/java/com/beautysalon/Controller/SmsController.java).

#### A. Disparo Avulso de Mensagem de Texto
* **Método**: `POST`
* **URL**: `/api/sms/enviar`
* **Parâmetros de Formulário / URL-Encoded (`@RequestParam`)**:
  * `telefone` (`String`, obrigatório): Número do celular de destino com DDD (ex: `11999998888`).
  * `mensagem` (`String`, obrigatório, max 160 caracteres): Conteúdo da mensagem a ser enviada.
* **Exemplo cURL**:
```bash
curl -X POST "http://localhost:8081/api/sms/enviar" \
     -d "telefone=11999998888" \
     -d "mensagem=Lembrete: Seu agendamento no BeautySalon e hoje as 15:00."
```
* **Resposta Sucesso (200 OK)**:
```text
{"status":"success","message":"SMS enviado"}
```
* **Resposta Erro (500 Internal Server Error)**: `Falha ao enviar SMS: <detalhes>`

---

### 2.4. API de Cotação de Moeda (`/api/cotacao`)

Gerenciada por [QuotationController](file:///c:/Dev/Projetos/Proj_Studio/src/main/java/com/beautysalon/Controller/QuotationController.java).

#### A. Consultar Cotação Atual do Dólar
* **Método**: `GET`
* **URL**: `/api/cotacao`
* **Resposta Esperada**:
```text
Cotação atual do dólar: R$ 5.48
```

> [!CAUTION]
> O endpoint atual apresenta chamada recursiva infinita em seu método estático (`StackOverflowError`). A correção arquitetural está documentada no relatório técnico.

---

### 2.5. API de Teste / Healthcheck (`/api/teste`)

Gerenciada por [TesteController](file:///c:/Dev/Projetos/Proj_Studio/src/main/java/com/beautysalon/API/TesteController.java).

#### A. Verificação de Disponibilidade
* **Método**: `GET`
* **URL**: `/api/teste`
* **Resposta Sucesso (200 OK)**:
```text
Working!
```

---

## 3. Endpoints de Integração AJAX em Controladores Web

Além dos controladores na pasta `API/`, alguns controladores MVC expõem endpoints com `@ResponseBody` para chamadas assíncronas feitas pela interface:

| Rota | Método | Descrição | Formato Resposta |
| :--- | :---: | :--- | :---: |
| `/roles/api` | `GET` | Retorna lista de roles cadastradas para popular comboboxes via AJAX | `JSON (List<RoleDTO>)` |
| `/agendamentos/deletar/{id}` | `POST` | Exclusão assíncrona de agendamento por ID com atualização da tabela | `200 OK` (sem corpo) |
| `/clientes/{id}/enviar-sms-confirmacao` | `POST` | Gera código aleatório de 6 dígitos e envia SMS de confirmação | `200 OK (Texto)` |
| `/register` | `POST` | Cadastro assíncrono de novo usuário com retorno de validação | `JSON {success, errors}` |

---

## 4. Catálogo de Rotas Web (Thymeleaf MVC)

### 4.1. Módulo de Autenticação e Entrada
* `GET /`: Página de entrada / dashboard inicial (requer autenticação).
* `GET /home`: Redirecionamento e visualização da dashboard principal.
* `GET /login`: Formulário de autenticação de operadores.
* `GET /register`: Formulário de criação de conta pública.
* `POST /logout`: Encerramento da sessão autenticada.

### 4.2. Módulo de Clientes (`/clientes`)
* `GET /clientes`: Tabela com a lista completa de clientes cadastrados.
* `GET /clientes/novo`: Exibe tela de cadastro de novo cliente.
* `POST /clientes`: Persiste novo cliente ou alterações via formulário multipart.
* `GET /clientes/edit/{id}`: Formulário para alteração de dados de cliente.
* `GET /clientes/delete/{id}`: Exclusão de cliente por ID via requisição GET.
* `GET /clientes/pesquisar?nome={termo}`: Filtra clientes cujo nome contenha o termo pesquisado.

### 4.3. Módulo de Agendamentos (`/agendamentos`)
* `GET /agendamentos`: Tabela com visão cronológica de atendimentos agendados.
* `GET /agendamentos/novo`: Formulário para marcação de atendimento (seleciona cliente e serviço).
* `POST /agendamentos`: Persiste o agendamento validando conflitos e integridade.
* `GET /agendamentos/{id}`: Visualização e detalhe de agendamento específico.
* `POST /agendamentos/{id}/atualizar`: Processa alteração de data/hora ou serviço de agendamento.
* `GET /agendamentos/servico/{id}`: Filtra todos os agendamentos vinculados a um serviço específico.

### 4.4. Módulo de Serviços (`/servicos`)
* `GET /servicos`: Catálogo de procedimentos com exibição de cards/tabela, preços e imagens.
* `GET /servicos/novo`: Formulário para cadastro de serviço com campo de upload de imagem.
* `POST /servicos`: Criação de serviço salvando arquivo no diretório de uploads.
* `GET /servicos/edit/{id}`: Formulário de edição dos dados e substituição de imagem de serviço.
* `POST /servicos/{id}/atualizar`: Processa atualização das informações do serviço.
* `GET /servicos/delete/{id}`: Exclui o serviço do catálogo.

### 4.5. Módulo de Usuários e Operadores (`/usuarios`)
* `GET /usuarios`: Listagem de usuários operadores do sistema.
* `GET /usuarios/novo`: Formulário para cadastro administrativo de novo usuário com atribuição de perfil.
* `POST /usuarios`: Persistência de novo usuário com criptografia de senha e upload de avatar.
* `GET /usuarios/{id}`: Edição de dados e permissões de usuário.
* `POST /usuarios/{id}/atualizar`: Processa atualização de dados de operador.
* `POST /usuarios/{id}/deletar`: Exclui usuário do sistema.

### 4.6. Módulo de Papéis e Permissões (`/roles`) *(Requer perfil ADMIN)*
* `GET /roles`: Listagem das autoridades de acesso configuradas no sistema.
* `GET /roles/novo`: Formulário de cadastro de novo papel.
* `POST /roles/salvar`: Persistência de nova autoridade (ex: `GERENTE`, `RECEPCIONISTA`).
* `POST /roles/{id}/atualizar`: Atualização do nome do papel.
* `POST /roles/{id}/deletar`: Exclusão de papel por ID.

---

## 5. Códigos de Status HTTP

O sistema BeautySalon utiliza as seguintes convenções de códigos de retorno HTTP:

* **200 OK**: Requisição processada com êxito e corpo retornado.
* **204 No Content**: Operação de exclusão processada com sucesso sem conteúdo de retorno.
* **302 Found (Redirect)**: Redirecionamento pós-submissão de formulários MVC (padrão Post/Redirect/Get).
* **400 Bad Request**: Erros de validação em campos obrigatórios do formulário ou DTO.
* **401 Unauthorized**: Acesso a rota protegida sem autenticação prévia.
* **403 Forbidden**: Tentativa de acesso a rota protegida sem o perfil necessário (ex: usuário comum acessando `/roles/**`).
* **404 Not Found**: Recurso inexistente (capturado e tratado pelo `GlobalExceptionHandler`).
* **500 Internal Server Error**: Exceção não prevista no servidor, tratada pela página de erro customizada (`/error/customError`).
