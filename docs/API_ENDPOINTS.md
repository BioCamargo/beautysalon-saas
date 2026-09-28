# 📡 Catálogo de Endpoints e Rotas - BeautySalon (LUMORA SaaS)

Este documento cataloga integralmente as **APIs RESTful (OpenAPI 3 / Swagger)** e as **Rotas Web (MVC/Thymeleaf)** do sistema **BeautySalon** (`Proj_Studio`), detalhando parâmetros, cabeçalhos, payloads JSON, permissões de acesso e contratos multi-tenant.

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
  * Rotas sob `/api/**` e Swagger `/v3/api-docs/**`, `/swagger-ui/**` são configuradas no `SecurityConfig` com CSRF desabilitado para consumo de clientes REST/mobile.

---

## 2. Documentação OpenAPI / Swagger

O projeto utiliza **Springdoc OpenAPI 3 (v2.8.5)** compatível com Spring Boot 3.5.x.
Acesse pelo navegador:
```
http://localhost:8081/swagger-ui/index.html
```

---

## 3. Endpoints da API RESTful v1 (`/api/v1/{slug}/...`)

Todas as rotas REST recebem o parâmetro `{slug}` da empresa/tenant na URL, garantindo isolamento total de dados.

### 3.1. API de Clientes (`/api/v1/{slug}/clientes`)
Gerenciada por `ClienteRestController`.

| Método | Endpoint | Descrição | Status Sucesso |
| :---: | :--- | :--- | :---: |
| `GET` | `/api/v1/{slug}/clientes` | Listagem de todos os clientes do tenant | `200 OK` |
| `GET` | `/api/v1/{slug}/clientes/{id}` | Busca de cliente específico por ID | `200 OK` / `404 Not Found` |
| `POST` | `/api/v1/{slug}/clientes` | Cadastro de novo cliente | `201 Created` |
| `PUT` | `/api/v1/{slug}/clientes/{id}` | Atualização cadastral de cliente | `200 OK` |
| `DELETE` | `/api/v1/{slug}/clientes/{id}` | Remoção lógica/física de cliente | `204 No Content` |

**Exemplo Payload Cadastro Cliente (`POST`):**
```json
{
  "nome": "Mariana Ferreira",
  "email": "mariana@gmail.com",
  "telefone": "11965432100",
  "cpf": "123.456.789-00",
  "dataNascimento": "1992-05-14"
}
```

---

### 3.2. API de Agendamentos (`/api/v1/{slug}/agendamentos`)
Gerenciada por `AgendamentoRestController`.

| Método | Endpoint | Descrição | Status Sucesso |
| :---: | :--- | :--- | :---: |
| `GET` | `/api/v1/{slug}/agendamentos` | Lista agendamentos filtrados por data ou período | `200 OK` |
| `POST` | `/api/v1/{slug}/agendamentos` | Cria agendamento validando disponibilidade de horário | `201 Created` |
| `PATCH` | `/api/v1/{slug}/agendamentos/{id}/status` | Altera status (`PENDENTE`, `CONFIRMADO`, `FINALIZADO`, `CANCELADO`) | `200 OK` |

---

### 3.3. API de Estoque & Produtos (`/api/v1/{slug}/estoque`)
Gerenciada por `EstoqueRestController`.

| Método | Endpoint | Descrição | Status Sucesso |
| :---: | :--- | :--- | :---: |
| `GET` | `/api/v1/{slug}/estoque` | Lista produtos cadastrados (revenda e insumos) | `200 OK` |
| `GET` | `/api/v1/{slug}/estoque/critico` | Alertas de produtos abaixo do estoque mínimo | `200 OK` |
| `POST` | `/api/v1/{slug}/estoque` | Cadastro de novo produto no catálogo | `201 Created` |
| `POST` | `/api/v1/{slug}/estoque/{id}/movimentar` | Entrada, saída avulsa ou ajuste de balanço | `200 OK` |

---

### 3.4. API de Financeiro & Comandas (`/api/v1/{slug}/financeiro`)
Gerenciada por `FinanceiroRestController`.

| Método | Endpoint | Descrição | Status Sucesso |
| :---: | :--- | :--- | :---: |
| `GET` | `/api/v1/{slug}/financeiro/caixa-atual` | Consulta status e saldo do caixa aberto | `200 OK` |
| `POST` | `/api/v1/{slug}/financeiro/comandas` | Abertura de nova comanda de atendimento | `201 Created` |
| `POST` | `/api/v1/{slug}/financeiro/comandas/{id}/itens` | Adiciona serviço ou produto de revenda | `200 OK` |
| `POST` | `/api/v1/{slug}/financeiro/comandas/{id}/fechar` | Fechamento com baixa de estoque e comissões | `200 OK` |

---

## 4. Catálogo de Rotas Web (Thymeleaf MVC)

| Rota | Método | Acesso | Descrição |
| :--- | :---: | :---: | :--- |
| `/{slug}/home` | `GET` | Autenticado | Dashboard principal com KPIs operacionais e agenda de hoje |
| `/{slug}/financeiro` | `GET` | Autenticado | Gestão de caixa, sangrias, reforços e listagem de comandas |
| `/{slug}/financeiro/comandas/{id}` | `GET` | Autenticado | Painel de controle da comanda com itens e pagamentos |
| `/{slug}/estoque` | `GET` | Autenticado | Controle de estoque, valor imobilizado e alertas mínimos |
| `/{slug}/fidelizacao` | `GET` | Autenticado | Radar anti-churn (30+ dias), aniversariantes, cupons e vouchers |
| `/{slug}/agendamentos` | `GET` | Autenticado | Tabela de horários e calendário interativo FullCalendar |
| `/{slug}/clientes` | `GET` | Autenticado | CRM de clientes e histórico de visitas |
| `/{slug}/clientes/{id}/anamnese` | `GET`, `POST` | Autenticado | Ficha de anamnese técnica digital |
| `/{slug}/servicos` | `GET` | Autenticado | Catálogo de procedimentos e ficha técnica de insumos |
| `/{slug}/relatorios` | `GET` | **ADMIN/OWNER** | Relatórios de DRE, faturamento, comissões e métricas |
| `/{slug}/usuarios` | `GET` | **ADMIN/OWNER** | Gestão de operadores, profissionais e comissões |
| `/{slug}/agendar` | `GET`, `POST` | **Público** | Portal online de autoatendimento para clientes finais |
| `/login` | `GET`, `POST` | Público | Autenticação no sistema |
| `/register` | `GET`, `POST` | Público | Cadastro de novo usuário |
| `/register-empresa` | `GET`, `POST` | Público | Onboarding e criação de novo Tenant SaaS |

---

## 5. Tratamento Global de Erros REST (ProblemDetails)

Erros ocorridos nas APIs REST são interceptados pelo `GlobalRestExceptionHandler`, retornando a estrutura padronizada RFC 7807:

```json
{
  "type": "about:blank",
  "title": "Recurso não encontrado",
  "status": 404,
  "detail": "Cliente não localizado para o ID informado no tenant lumora",
  "instance": "/api/v1/lumora/clientes/999",
  "timestamp": "2026-09-28T19:00:00"
}
```
