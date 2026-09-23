[![Review Assignment Due Date](https://classroom.github.com/assets/deadline-readme-button-22041afd0340ce965d47ae6ef1cefeee28c7c493a6346c4f15d667ab976d596c.svg)](https://classroom.github.com/a/_-p6TcGH)

# ✂️ BeautySalon / LUMORA - Sistema Completo de Gestão para Salão de Beleza, Estética & Barbearias (Multi-Tenant SaaS)

![Java](https://img.shields.io/badge/Java-17%20%2F%2025-orange.svg?logo=java)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.0-brightgreen.svg?logo=springboot)
![Spring Security](https://img.shields.io/badge/Spring%20Security-6-green.svg?logo=springsecurity)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-42.7.5-blue.svg?logo=postgresql)
![Thymeleaf](https://img.shields.io/badge/Thymeleaf-SSR-005F0F.svg?logo=thymeleaf)
![Swagger](https://img.shields.io/badge/OpenAPI%20%2F%20Swagger-v3-85EA2D.svg?logo=swagger)
![Maven](https://img.shields.io/badge/Build-Maven-C71A36.svg?logo=apachemaven)

O **BeautySalon** (LUMORA / Proj_Studio) é uma plataforma web empresarial completa desenvolvida com **Spring Boot 3.5**, projetada para otimizar os processos operacionais, financeiros e de fidelização de salões de beleza, clínicas de estética e barbearias com arquitetura **Multi-Tenant SaaS** (isolamento de dados por empresa via rota `/{slug}/`).

---

## 📌 Sumário

1. [Módulos e Funcionalidades Principais](#-módulos-e-funcionalidades-principais)
2. [Arquitetura e Tecnologias](#-arquitetura-e-tecnologias)
3. [Documentação OpenAPI / Swagger](#-documentação-openapi--swagger)
4. [Estrutura do Banco de Dados](#-estrutura-do-banco-de-dados)
5. [Estrutura do Projeto](#-estrutura-do-projeto)
6. [Início Rápido (Quick Start)](#-início-rápido-quick-start)
7. [Contas e Dados Padrão](#-contas-e-dados-padrão)
8. [Resumo das Rotas da Aplicação](#-resumo-das-rotas-da-aplicação)
9. [Documentação Complementar](#-documentação-complementar)

---

## 🌟 Módulos e Funcionalidades Principais

### 1. 💼 Módulo Financeiro & Caixa
* **Abertura e Fechamento de Caixa Diário**:
  * Definição de saldo inicial / fundo de troco.
  * Lançamento de movimentações rápidas: Sangria, Reforço e Despesas Operacionais.
  * Fechamento com conferência de valores (saldo esperado vs. saldo contado) e cálculo de sobras/faltas.
* **Comandas de Atendimento**:
  * Comanda individual por cliente ou atendimento avulso/balcão.
  * Lançamento de serviços e produtos de revenda na mesma comanda.
  * Descontos, acréscimos e formas de pagamento flexíveis: PIX, Cartão de Crédito, Débito, Dinheiro e Voucher.
  * **Baixa Automática de Estoque**: produtos e insumos de serviços são baixados automaticamente na finalização da comanda.
  * Cálculo dinâmico de comissão devida ao profissional executor/vendedor.

---

### 2. 📦 Módulo de Estoque Inteligente
* **Produtos de Revenda & Consumo Interno**:
  * Controle de produtos para venda direta ao cliente e insumos de uso interno da equipe.
* **Capital Parado em Estoque**:
  * Cálculo em tempo real do montante financeiro imobilizado com base no preço de custo ($\sum \text{qtde} \times \text{custo}$).
* **Radar de Estoque Crítico / Mínimo**:
  * Alertas visuais para produtos atingindo a quantidade mínima de segurança.
  * Histórico e rastreabilidade completa de todas as movimentações (entradas, saídas e ajustes manuais).
* **Ficha Técnica de Serviços (`ServicoInsumo`)**:
  * Vínculo de insumos consumidos por procedimento para baixa automática.

---

### 3. ❤️ Fidelização de Clientes & CRM
* **Radar de Retenção de Clientes**:
  * Identificação automática de clientes ausentes há mais de 30 dias.
  * Botão de contato direto via **WhatsApp** com mensagem acolhedora pré-formatada.
* **Aniversariantes do Mês**:
  * Filtro automático de aniversariantes com atalho para envio de mensagem de parabéns e presente via WhatsApp.
* **Ficha e Linha do Tempo do Cliente**:
  * Histórico de visitas e atendimentos.
  * **Sugestão Inteligente**: identificação do serviço mais frequente e do profissional preferido.
* **Cupons de Desconto & Vouchers (Cartão Presente)**:
  * Criação de cupons promocionais (percentual ou valor fixo).
  * Emissão e controle de saldo restante de Cartões Presente.
  * Pacotes e combos de serviços com desconto agregado.

---

### 4. ✂️ Gestão de Profissionais & Equipe
* **Aproveitamento de Usuários como Profissionais**:
  * Cadastro unificado de operadores e especialistas (Cabeleireiro, Barbeiro, Manicure, Esteticista).
  * Definição de percentual de comissão padrão (`percentualComissao`), WhatsApp e cor personalizada na agenda.
* **Extrato de Comissões**:
  * Relatório detalhado de comissões geradas por profissional em qualquer período filtrado.

---

### 5. 🗓️ Agenda de Atendimentos
* **Quadro de Atendimentos de HOJE**:
  * Visão clara e instantânea no Dashboard dos clientes agendados no dia, horários, serviços e profissionais.
* **Visão em Tabela e Calendário Interativo**:
  * Alternância dinâmica entre listagem tabular com busca em tempo real e visualização semanal com FullCalendar.
* **Preservação de Dados**:
  * Suporte a seleção de profissional e integridade de data/hora na edição.

---

### 6. 📊 Dashboard & Relatórios Gerenciais
* **Dashboard em Tempo Real**:
  * Indicadores de status do caixa diário, atendimentos do dia, valor parado em estoque e aniversariantes do mês.
* **Relatórios Financeiros Executivos**:
  * Faturamento bruto, total de comissões pagas, lucro operacional líquido e ticket médio por atendimento.

---

## 🛠️ Arquitetura e Tecnologias

A aplicação utiliza arquitetura em camadas com Spring Boot, Spring Security 6 e renderização Server-Side (Thymeleaf), além de camada RESTful com OpenAPI / Swagger:

```
[ Navegador do Usuário / App Mobile ]
          │
          ├──► /{slug}/home (Dashboard Web Thymeleaf)
          ├──► /{slug}/financeiro, estoque, fidelizacao (MVC Web)
          ├──► /api/v1/{slug}/... (API RESTful JSON)
          └──► /swagger-ui/index.html (Documentação Interativa OpenAPI)
                      │
           ┌──────────▼──────────┐
           │  Spring Security 6  │  (Auth, CSRF, BCrypt, Roles OWNER/ADMIN/FUNCIONARIO)
           └──────────┬──────────┘
                      │
           ┌──────────▼──────────┐
           │  TenantInterceptor  │  (Captura /{slug}/ e popula TenantContext)
           └──────────┬──────────┘
                      │
           ┌──────────▼──────────┐
           │     Controllers     │  (MVC Controllers + @RestController)
           └──────────┬──────────┘
                      │
           ┌──────────▼──────────┐
           │    Service Layer    │  (FinanceiroService, EstoqueService, FidelizacaoService, etc.)
           └──────────┬──────────┘
                      │
           ┌──────────▼──────────┐
           │  Spring Data JPA    │  (Repositories com @EntityGraph e isolamento por empresaId)
           └──────────┬──────────┘
                      │
           ┌──────────▼──────────┐
           │   PostgreSQL / DB   │  (Supabase Cloud ou Local)
           └─────────────────────┘
```

---

## 📡 Documentação OpenAPI / Swagger

O sistema disponibiliza documentação interativa de todos os endpoints RESTful v1:
* **Swagger UI**: `http://localhost:8081/swagger-ui/index.html`
* **OpenAPI Specs JSON**: `http://localhost:8081/v3/api-docs`

---

## 🗄️ Estrutura do Banco de Dados

| Tabela | Entidade | Descrição / Finalidade |
| :--- | :--- | :--- |
| **`empresas`** | `Empresa` | Tenants do sistema SaaS (Salão/Studio, Slug na URL, CNPJ, contato). |
| **`"user"`** | `User` | Usuários do sistema e profissionais (Login, Senha, Role, Comissão %, Especialidade, Cor na Agenda). |
| **`clientes`** | `Cliente` | Base de clientes (Nome, E-mail, Telefone/WhatsApp, Data de Nascimento/Aniversário). |
| **`servicos`** | `Servico` | Catálogo de serviços e procedimentos (Nome, Preço, Duração, Comissão específica). |
| **`agendamentos`** | `Agendamento` | Agenda de atendimentos (Data/Hora, Cliente, Profissional, Status, Observações). |
| **`agendamento_servico`** | Associativa | Vínculo N:N entre Agendamento e Serviços contratados. |
| **`produtos`** | `Produto` | Catálogo de estoque (Revenda e Consumo, Custo, Venda, Estoque mínimo, Qtd atual). |
| **`servico_insumos`** | `ServicoInsumo` | Ficha técnica de serviços (vincula insumos consumidos por execução de serviço). |
| **`movimentacoes_estoque`**| `MovimentacaoEstoque` | Histórico e rastreabilidade de entradas, saídas por venda, consumo e ajustes. |
| **`caixas`** | `Caixa` | Fluxo de caixa diário (Saldo inicial, Entradas, Saídas/Sangrias, Saldo esperado, Fechamento). |
| **`comandas`** | `Comanda` | Comanda do cliente (Serviços + Produtos, Descontos, Forma de Pagamento e Status). |
| **`comanda_itens`** | `ComandaItem` | Itens da comanda (Serviço/Produto, Profissional executor/vendedor, Qtd, Comissão). |
| **`movimentacoes_financeiras`**| `MovimentacaoFinanceira` | Entradas avulsas, sangrias, reforços e despesas rápidas do caixa. |
| **`cupons_desconto`** | `CupomDesconto` | Cupons promocionais (Código, Percentual ou Valor fixo, Validade e Limite de usos). |
| **`vouchers_presente`** | `VoucherPresente` | Cartão presente / Vale presente (Código, Beneficiário, Saldo restante e Validade). |
| **`pacotes_combos`** | `PacoteCombo` | Combos promocionais de serviços com desconto agregado. |
| **`combo_servicos`** | Associativa | Vínculo N:N entre PacoteCombo e Serviços inclusos. |
| **`ausencias_profissionais`** | `AusenciaProfissional` | Folgas recorrentes, férias e intervalos para bloqueio de horários na agenda. |

---

## 📂 Estrutura do Projeto

```
Proj_Studio/
├── src/
│   ├── main/
│   │   ├── java/com/beautysalon/
│   │   │   ├── config/          # Segurança (SecurityConfig), OpenAPI e MVC (WebConfig)
│   │   │   ├── Controller/      # Controllers MVC Thymeleaf e Controllers RESTful (/api/v1/...)
│   │   │   ├── converter/       # Conversão DTO <-> Entity
│   │   │   ├── DTO/             # Objetos de Transferência de Dados e Records REST
│   │   │   ├── exception/       # Tratamento global de exceções MVC e REST
│   │   │   ├── Implementacao/   # Implementações de serviços e DataLoader
│   │   │   ├── Inteface/        # Contratos e interfaces de serviços
│   │   │   ├── model/           # Entidades de domínio JPA
│   │   │   ├── repository/      # Repositórios Spring Data JPA com isolamento multi-tenant
│   │   │   ├── service/         # Serviços especializados (Financeiro, Estoque, Fidelização, etc.)
│   │   │   ├── tenant/          # Interceptor e Contexto Multi-Tenant (ThreadLocal)
│   │   │   └── BeautySalonApplication.java
│   │   └── resources/
│   │       ├── static/          # Folhas de estilo (CSS), JavaScript e imagens
│   │       ├── templates/       # Templates Thymeleaf organizados por módulo
│   │       │   ├── agendamentos/
│   │       │   ├── auth/
│   │       │   ├── clientes/
│   │       │   ├── estoque/
│   │       │   ├── fidelizacao/
│   │       │   ├── financeiro/
│   │       │   ├── fragments/
│   │       │   ├── home/
│   │       │   ├── relatorios/
│   │       │   ├── servicos/
│   │       │   └── usuario/
│   │       └── application.properties
│   └── test/
├── docs/                        # Guias de Arquitetura, Banco, APIs e Plano de Estudos
├── pom.xml                      # Dependências Maven
└── mvnw / mvnw.cmd              # Wrapper do Maven
```

---

## 🚀 Início Rápido (Quick Start)

### Pré-requisitos
* **JDK 17** ou superior (testado e compatível até Java 25).
* **PostgreSQL** ou banco em nuvem (ex: Supabase).
* **Git**.

### 1. Clonar o Repositório
```bash
git clone https://github.com/BioCamargo/beautysalon-saas.git
cd beautysalon-saas
```

### 2. Executar a Aplicação
No Windows:
```cmd
.\mvnw.cmd spring-boot:run
```

No Linux / macOS:
```bash
./mvnw spring-boot:run
```

O servidor iniciará na porta **`8081`**. Acesse no navegador:
👉 [http://localhost:8081](http://localhost:8081)

---

## 🔑 Contas e Dados Padrão

* **Empresa de Demonstração (Tenant)**: `lumora` (URL: `http://localhost:8081/lumora/home`)
* **Usuário Administrador**: `admin`
* **Senha Inicial**: `admin123`
* **Papel (Role)**: `OWNER` / `ADMIN`

---

## 🗺️ Resumo das Rotas da Aplicação

| Rota | Método | Acesso | Descrição |
| :--- | :---: | :---: | :--- |
| `/{slug}/home` | `GET` | Autenticado | Dashboard principal com KPIs e Atendimentos de Hoje |
| `/{slug}/financeiro` | `GET` | Autenticado | Painel de Caixa diário e Comandas |
| `/{slug}/financeiro/comandas/{id}` | `GET` | Autenticado | Detalhe da Comanda com itens, produtos e recebimento |
| `/{slug}/estoque` | `GET` | Autenticado | Gestão de Estoque, produtos de revenda/consumo e alertas |
| `/{slug}/fidelizacao` | `GET` | Autenticado | Radar de retenção, aniversariantes, cupons e vouchers |
| `/{slug}/fidelizacao/cliente/{id}/historico` | `GET` | Autenticado | Linha do tempo e sugestão inteligente do cliente |
| `/{slug}/agendamentos` | `GET` | Autenticado | Agenda com tabela e calendário FullCalendar |
| `/{slug}/agendamentos/novo` | `GET`, `POST` | Autenticado | Cadastro de novo agendamento com profissional |
| `/{slug}/clientes` | `GET` | Autenticado | Base de clientes e contatos |
| `/{slug}/servicos` | `GET` | Autenticado | Catálogo de serviços e procedimentos |
| `/{slug}/relatorios` | `GET` | **ADMIN/OWNER** | Relatório de faturamento, comissões e ticket médio |
| `/{slug}/usuarios` | `GET` | **ADMIN/OWNER** | Gestão de usuários, comissões e equipe |
| `/api/v1/{slug}/clientes` | `REST` | Aberto | Endpoints REST de clientes com JSON e DTOs |
| `/api/v1/{slug}/agendamentos` | `REST` | Aberto | Endpoints REST de agendamentos e status |
| `/api/v1/{slug}/estoque` | `REST` | Aberto | Endpoints REST de produtos e movimentações |
| `/api/v1/{slug}/financeiro` | `REST` | Aberto | Endpoints REST de caixa e comandas |
| `/swagger-ui/index.html` | `GET` | Aberto | Interface interativa OpenAPI / Swagger |
| `/login` | `GET`, `POST` | Público | Tela de login no sistema |
| `/register` | `GET`, `POST` | Público | Cadastro de novo usuário |
| `/register-empresa` | `GET`, `POST` | Público | Cadastro de nova empresa/salão (novo tenant) |

---

## 📚 Documentação Complementar

Para aprofundar na arquitetura técnica, guias e alinhamento de portfólio, consulte a pasta [`/docs`](file:///c:/Dev/Projetos/Proj_Studio/docs):

* 📋 [**Plano de 15 Dias — Java Backend Moderno & Aderência**](file:///c:/Dev/Projetos/Proj_Studio/docs/PLANO_15_DIAS_JAVA_BACKEND.md)
* 🏛️ [**Arquitetura do Sistema**](file:///c:/Dev/Projetos/Proj_Studio/docs/ARQUITETURA.md)
* 🗄️ [**Estrutura do Banco de Dados & Dicionário**](file:///c:/Dev/Projetos/Proj_Studio/docs/BANCO_DE_DADOS.md)
* 🔒 [**Segurança e Autenticação**](file:///c:/Dev/Projetos/Proj_Studio/docs/SEGURANCA_E_AUTENTICACAO.md)
* 🌐 [**Catálogo de Endpoints e Rotas**](file:///c:/Dev/Projetos/Proj_Studio/docs/API_ENDPOINTS.md)
* 🚀 [**Guia de Instalação e Execução**](file:///c:/Dev/Projetos/Proj_Studio/docs/GUIA_DE_INSTALACAO_E_EXECUCAO.md)
* 📈 [**Análise Técnica e Melhorias**](file:///c:/Dev/Projetos/Proj_Studio/docs/ANALISE_TECNICA_E_MELHORIAS.md)
