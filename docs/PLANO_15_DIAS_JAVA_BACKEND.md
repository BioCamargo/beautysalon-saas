# 📋 Plano de 15 Dias — Java Backend Moderno & Aderência do Projeto

Este documento detalha o mapeamento completo do roteiro prático de estudos e portfólio **"Plano de 15 Dias — Java Backend Moderno"** comparado ao estado atual e futuro da aplicação **BeautySalon / LUMORA (`Proj_Studio`)**.

---

## 🎯 1. Objetivo do Plano
Consolidar a experiência prática no ecossistema Java moderno e construir um projeto SaaS demonstrável, robusto e arquiteturalmente preparado para entrevistas técnicas de alto nível para vagas de Desenvolvedor Backend Java.

---

## 📊 2. Matriz de Aderência do Projeto (BeautySalon)

| Categoria / Dia | Tópicos do Plano de 15 Dias | Status no Projeto | Detalhamento da Implementação |
| :--- | :--- | :---: | :--- |
| **Dia 1 — Ambiente & Ferramentas** | Java 17+, Maven, Git/GitHub, Boas Práticas | ✅ **Concluído** | Java 17/25 configurado, Maven Wrapper (`mvnw`), estrutura padrão Spring e versionamento Git. |
| **Dia 2 — Spring Boot Core** | Spring Boot 3.x, Spring Web, Controllers, Services | ✅ **Concluído** | Spring Boot 3.5.0, divisão clara de responsabilidades (`@Controller`, `@Service`, `@Repository`). |
| **Dia 3 — REST & HTTP** | Métodos HTTP (GET, POST, PUT, DELETE), Status HTTP, JSON | 🔄 **Em Evolução** | Sistema possui rotas web completas (MVC/Thymeleaf). Camada de API REST pura (`/api/v1/...`) planejada para consumo externo/mobile. |
| **Dia 4 — JPA & Hibernate** | Entidades, Relacionamentos, Repositórios, Consultas | ✅ **Concluído** | Mapeamento com `@Entity`, `@ManyToOne`, `@OneToMany`, `@EntityGraph`, consultas HQL customizadas. |
| **Dia 5 — Banco de Dados** | PostgreSQL, DDL/DML, DBeaver, Índices e Queries | ✅ **Concluído** | Driver PostgreSQL 42.7.5 ativo, suporte a funções nativas (`EXTRACT`), isolamento multi-tenant. |
| **Dia 6 — DTO, Validação & Tratamento de Erros** | DTOs (Java Records), Bean Validation, `@ControllerAdvice` | 🔄 **Em Evolução** | Bean Validation (`@Valid`, `@NotBlank`, `@DecimalMin`) aplicado nos modelos. Falta padronização de DTOs Records na camada REST. |
| **Dia 7 — Segurança & Autenticação** | Spring Security, BCrypt, Roles/Perfis, Proteção de Rotas, JWT | 🟡 **Parcial** | Spring Security 6 com autenticação baseada em banco de dados, `BCryptPasswordEncoder` e controle de perfil. Suporte a JWT planejado para a API REST. |
| **Dia 8 — Testes Automatizados** | JUnit 5, Mockito, Testes Unitários dos Services | ⏳ **Pendente** | Dependências `spring-boot-starter-test` e `spring-security-test` presentes. Falta escrever os testes dos Services principais. |
| **Dia 9 — SOLID, Clean Code & Patterns** | Princípios SOLID, Injeção de Dependência, Patterns de Negócio | ✅ **Concluído** | Princípio da responsabilidade única em Services dedicados (`EstoqueService`, `FinanceiroService`, `FidelizacaoService`). |
| **Dia 10 — Arquitetura de Software** | Separação em camadas, Clean Architecture, Arquitetura Hexagonal | ✅ **Concluído** | Estrutura limpa e desacoplada em camadas lógicas bem definidas. |
| **Dia 11 — Containers & Docker** | Dockerfile, Docker Compose (App + PostgreSQL) | ⏳ **Pendente** | Criação do `Dockerfile` multi-stage build e arquivo `docker-compose.yml`. |
| **Dia 12 — Mensageria Assíncrona** | RabbitMQ (Filas, Eventos, Producer & Consumer), Kafka conceitual | ⏳ **Pendente** | Integração com RabbitMQ para eventos assíncronos (ex: alerta de estoque baixo, confirmação de agendamento). |
| **Dia 13 — CI/CD Pipeline** | GitHub Actions / Jenkins, Build automático, Testes | ⏳ **Pendente** | Pipeline do GitHub Actions (`.github/workflows/maven.yml`) para build e validação contínua. |
| **Dia 14 — Cloud (AWS)** | Conceitos de nuvem: IAM, EC2, RDS, S3, CloudWatch, SQS | ⏳ **Pendente** | Documentação conceitual e guia de deploy na AWS (EC2/RDS/S3). |
| **Dia 15 — Portfólio, GitHub & Entrevistas** | README profissional, Documentação técnica e Roteiro de entrevistas | ✅ **Concluído** | `README.md` completo, documentações na pasta `/docs`, guias de arquitetura e segurança. |

---

## 🏢 3. Módulos de Negócio (LUMORA / BeautySalon)

O plano especifica os módulos de negócio obrigatórios para o portfólio. O projeto implementou todos e expandiu com funcionalidades avançadas:

### 1. Multi-tenant (Isolamento por Empresa)
- **Especificado:** Cadastro e isolamento de dados por empresa.
- **Implementado:** Isolamento via path variable `/{slug}/...`, resolução dinâmica por `EmpresaRepository` e vínculo de tenant em todas as tabelas.

### 2. Clientes & CRM
- **Especificado:** Cadastro, consulta, atualização e histórico.
- **Implementado:** CRUD completo, histórico unificado de atendimentos, alerta de clientes inativos/sumidos e aniversariantes do mês.

### 3. Agenda de Atendimentos
- **Especificado:** Agendamentos e organização de horários.
- **Implementado:** Painel diário de horários marcados, vínculo com cliente e profissional, status de atendimento e observações.

### 4. Estoque & Produtos
- **Especificado:** Cadastro, controle de entradas, saídas e consumo.
- **Implementado:** Separação entre produtos de revenda e consumo interno, baixa automática via insumos de serviços, cálculo de capital financeiro parado em estoque e alerta de estoque mínimo.

### 5. Financeiro & Caixa
- **Especificado:** Receitas, despesas e movimentações financeiras.
- **Implementado:** Fluxo de abertura/fechamento de caixa com conferência de saldo físico, comandas de atendimento com múltiplos itens, cálculo automático de comissão de profissionais e conciliação de receitas/despesas.

### 6. Módulo Avançado de Fidelização *(Diferencial Implementado)*
- Cupons de Desconto com regras de validade e valor mínimo.
- Vouchers / Cartões Presente com saldo debitável.
- Pacotes e Combos promocionais de serviços.

---

## 🗺️ 4. Roadmap de Implementação para 100% de Aderência

Para cobrir integralmente os tópicos pendentes do plano de 15 dias, o seguinte roteiro será seguido nas próximas fases:

```mermaid
graph TD
    A[Fase 1: Módulos de Negócio & Telas] -->|Concluído| B[Fase 2: Testes Unitários JUnit 5 + Mockito]
    B --> C[Fase 3: API REST DTOs + Swagger / OpenAPI]
    C --> D[Fase 4: Autenticação JWT]
    D --> E[Fase 5: Docker & Docker Compose]
    E --> F[Fase 6: Mensageria RabbitMQ]
    F --> G[Fase 7: CI/CD com GitHub Actions]
```

### Detalhes das Próximas Etapas:
1. **Fase 2 — Testes Automatizados (Dia 8):**
   * Escrever testes unitários com JUnit 5 e Mockito para `FinanceiroService`, `EstoqueService` e `FidelizacaoService`.
2. **Fase 3 — API REST & Swagger (Dias 3 e 6):**
   * Implementar endpoints REST (`/api/v1/...`), DTOs com Java Records e documentação OpenAPI com Swagger UI (`/swagger-ui.html`).
3. **Fase 4 — Autenticação JWT (Dia 7):**
   * Filtro `OncePerRequestFilter`, geração de Token JWT e endpoint `/api/auth/login`.
4. **Fase 5 — Dockerização (Dia 11):**
   * Criar `Dockerfile` multi-stage build e `docker-compose.yml` integrando `app` + `postgres` + `rabbitmq`.
5. **Fase 6 — Mensageria RabbitMQ (Dia 12):**
   * Filas para notificações assíncronas (ex: envio de e-mails, alertas de estoque baixo).
6. **Fase 7 — GitHub Actions CI/CD (Dia 13):**
   * Pipeline de automação de testes e build a cada commit/push.
