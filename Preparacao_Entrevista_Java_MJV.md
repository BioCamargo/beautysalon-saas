# 🎯 Guia Definitivo de Preparação para Entrevista Técnica Java / SaaS
**Candidato:** Desenvolvedor Java / SaaS  
**Empresa Alvo:** MJV Technology & Innovation (e grandes consultorias/empresas tech)  
**Projeto Base:** `BeautySalon SaaS` (Java 17, Spring Boot 3.x, JPA/Hibernate, Spring Security, Multi-tenancy, JUnit/Mockito, Swagger)  
**Data:** 2026

---

## 📑 Sumário
1. [Pitch de Apresentação (O Roteiro Campeão)](#1-pitch-de-apresentação)
2. [Raio-X Técnico do Projeto BeautySalon](#2-raio-x-técnico-do-projeto-beautysalon)
3. [Simulação de Entrevista: Perguntas & Respostas Estratégicas](#3-simulação-de-entrevista-perguntas--respostas)
   - [Módulo A: Arquitetura & Multi-Tenancy](#módulo-a-arquitetura--multi-tenancy)
   - [Módulo B: Persistência, JPA & Performance (N+1, EntityGraph)](#módulo-b-persistência-jpa--performance)
   - [Módulo C: Regras de Negócio, Concorrência & Transações](#módulo-c-regras-de-negócio-concorrência--transações)
   - [Módulo D: Segurança (Spring Security 6 & RBAC)](#módulo-d-segurança-spring-security-6--rbac)
   - [Módulo E: Qualidade, Testes Automatizados & Mensageria](#módulo-e-qualidade-testes-automatizados--mensageria)
   - [Módulo F: Escalabilidade & Práticas Corporativas](#módulo-f-escalabilidade--práticas-corporativas)
4. [Dicas Comportamentais & Postura de Tech Lead / Sênior](#4-dicas-comportamentais--postura)

---

## 1. Pitch de Apresentação

Quando o entrevistador disser: *"Me conte sobre você, sua experiência e sobre o projeto BeautySalon que está no seu GitHub."*

### 🗣️ Roteiro Pronto para Falar (Memorize a estrutura):

> "Com certeza! Sou desenvolvedor focado no ecossistema Java moderno e venho trabalhando em soluções corporativas robustas. 
>
> Recentemente projetei e desenvolvi o **BeautySalon**, uma plataforma **SaaS Multi-Tenant** completa para gestão de salões e clínicas de estética. O sistema cobre desde agendamento em tempo real, controle de comandas, comissões de profissionais, controle de estoque com baixa automática, até relatórios financeiros consolidados.
>
> Do ponto de vista técnico, estruturei a aplicação com **Java 17 e Spring Boot 3**, utilizando:
> - **Multi-Tenancy Discriminator-based** seguro, propagado via `ThreadLocal` (`TenantContext`) e interceptores HTTP;
> - **Spring Data JPA com Hibernate 6**, aplicando `@EntityGraph` para eliminar gargalos de `N+1` em relacionamentos complexos de comandas e itens;
> - **Spring Security 6** com controle de acesso granular baseado em papéis (RBAC);
> - **Testes unitários automatizados** com **JUnit 5 e Mockito**, cobrindo fluxos críticos de negócio;
> - **Documentação viva de API** com **OpenAPI/Swagger** e desacoplamento de eventos para integrações assíncronas (como mensageria/WhatsApp).
>
> Meu foco ao desenhar o projeto foi garantir os mesmos padrões de manutenibilidade, segurança e performance exigidos em projetos de missão crítica, como os desenvolvidos na **MJV**."

---

## 2. Raio-X Técnico do Projeto BeautySalon

Tenha esses detalhes do seu código na ponta da língua:

| Componente | Implementação no BeautySalon | Por que foi feito assim? |
| :--- | :--- | :--- |
| **Multi-Tenancy** | `TenantContext` (`ThreadLocal`) + `TenantInterceptor` + filtro por `empresaId` nos Repositories | Isolamento lógico seguro entre diferentes salões sem o overhead de gerenciar centenas de bancos de dados. |
| **Performance JPA** | `@EntityGraph(attributePaths = {"empresa", "cliente", "caixa", "itens.profissional", ...})` | Evita consultas extras (problema do `N+1`), fazendo fetch em um único JOIN eficiente. |
| **Consistência Financeira** | `sumFaturamentoPorPeriodo`, `COALESCE`, consultas com agregação atômica | Cálculos financeiros precisos e atômicos delegados ao banco de dados, protegidos por `@Transactional`. |
| **Segurança** | `SecurityFilterChain` (Spring Security 6) com CSRF, RBAC e sessões protegidas | Padrão moderno sem classes legadas (`WebSecurityConfigurerAdapter` descontinuado). |
| **Testabilidade** | `AgendamentoServiceImplTest`, `EstoqueServiceTest` com Mockito | Testes rápidos, sem dependência de banco de dados real em memória para validação unitária de regras. |

---

## 3. Simulação de Entrevista: Perguntas & Respostas

### Módulo A: Arquitetura & Multi-Tenancy

#### P1. Qual estratégia de Multi-Tenancy você adotou no BeautySalon e quais os prós e contras dela?
* **Resposta Ideal:**
  > "Adotei a estratégia de **Tabelas Compartilhadas com Coluna Discriminadora (`empresa_id`)**. 
  > 
  > **Vantagens:** Menor custo de infraestrutura, facilidade para gerenciar migrações de banco e facilidade de escala inicial.
  > 
  > **Cuidados:** Exige garantia absoluta de isolamento. Para isso, criei um `TenantInterceptor` que identifica a empresa via header/slug, popula o `TenantContext` via `ThreadLocal` e garante que todos os Repositories e Services filtrem por `empresaId`. 
  > 
  > **Ponto crítico:** No método `afterCompletion` do interceptor, eu executo `TenantContext.clear()` para evitar **Memory Leaks** e **Data Leak** caso as threads do pool HTTP do Tomcat sejam reutilizadas."

---

### Módulo B: Persistência, JPA & Performance

#### P2. Como você lidou com o problema de N+1 Queries no carregamento das Comandas e seus Itens?
* **Resposta Ideal:**
  > "Em entidades ricas como a `Comanda`, que possui múltiplos relacionamentos (`Cliente`, `Caixa`, `Agendamento`, `Itens`, `Profissional`, `Servico`, `Produto`), o lazy loading padrão gera o problema do N+1 (uma query para a comanda e dezenas de queries para cada item/serviço).
  > 
  > No `ComandaRepository`, utilizei a anotação `@EntityGraph(attributePaths = {...})`. Isso instrui o Hibernate a gerar um `LEFT JOIN FETCH` otimizado em uma única query SQL, trazendo a comanda e suas dependências de forma performática sem sobrecarregar a conexão com o banco."

#### P3. Por que você utilizou `COALESCE` nas queries de agregação financeira (como faturamento e comissões)?
* **Resposta Ideal:**
  > "Se uma empresa recém-cadastrada ou um período consultado não possuir nenhuma venda fechada, o `SUM()` nativo do SQL retorna `NULL`. O uso do `COALESCE(SUM(c.valorTotal), 0)` garante que o retorno seja sempre `0` (BigDecimal zero), evitando `NullPointerException` na camada de serviço ou necessidade de verificações manuais desnecessárias no Java."

---

### Módulo C: Regras de Negócio, Concorrência & Transações

#### P4. Como você garante que dois clientes não agendem o mesmo profissional no mesmo horário simultaneamente?
* **Resposta Ideal:**
  > "Na camada de serviço (`AgendamentoServiceImpl`), antes de persistir, executo uma validação consultando se o profissional já possui agendamento ativo com sobreposição de horário (`dataInicio` e `dataFim`).
  > 
  > Para ambientes com alta concorrência, a estratégia ideal complementada no projeto é o uso de **Constraint Única composta no banco** (`empresa_id`, `profissional_id`, `data_hora`) ou **Lock Otimista (`@Version`)** na entidade do profissional/agenda, garantindo que o segundo commit falhe com `OptimisticLockException` e retorne um erro amigável ao cliente."

#### P5. Como você utiliza o `@Transactional` no fechamento de comandas e baixa de estoque?
* **Resposta Ideal:**
  > "O fechamento de uma comanda envolve múltiplos passos: alteração do status da comanda, baixa no estoque dos produtos utilizados, lançamento das comissões do profissional e atualização do caixa da empresa.
  > 
  > Essas operações são executadas sob `@Transactional(rollbackFor = Exception.class)`. Se a baixa de estoque falhar por falta de saldo, toda a transação sofre rollback, impedindo que a comanda seja marcada como paga com estado inconsistente no banco de dados."

---

### Módulo D: Segurança (Spring Security 6 & RBAC)

#### P6. O que mudou no Spring Security 6 que você aplicou no projeto?
* **Resposta Ideal:**
  > "No Spring Security 6 (Spring Boot 3), a antiga classe `WebSecurityConfigurerAdapter` foi removida. Toda a configuração agora é baseada em injeção de beans de `SecurityFilterChain` com sintaxe funcional e lambdas (`authorizeHttpRequests(auth -> auth...)`).
  > 
  > Configurei autenticação baseada em sessão com isolamento de rotas por perfis (`ADMIN`, `PROFISSIONAL`, `RECEPCAO`), garantindo que apenas usuários autorizados tenham acesso aos módulos de relatórios e configurações financeiras."

---

### Módulo E: Qualidade, Testes Automatizados & Mensageria

#### P7. Como você estruturou a suíte de testes unitários do sistema?
* **Resposta Ideal:**
  > "Utilizei **JUnit 5** e **Mockito** com a anotação `@ExtendWith(MockitoExtension.class)`.
  > 
  > Mocko os repositórios e serviços externos (como `WhatsAppService` e mensageria) com `@Mock` e injeto na classe sob teste com `@InjectMocks`. Nos testes (`AgendamentoServiceImplTest`), testo cenários de sucesso e exceções de negócio (ex: agendamento com dados inválidos, entidades não encontradas e conflito de horário), além de garantir que o `TenantContext` seja mockado e limpo a cada teste no `@BeforeEach` e `@AfterEach`."

#### P8. Como o sistema lida com envio de lembretes e notificações sem travar a requisição do usuário?
* **Resposta Ideal:**
  > "Operações de notificação (como envio de WhatsApp ou e-mail de confirmação) não devem bloquear a thread HTTP do agendamento. 
  > 
  > Projetei a arquitetura desacoplada utilizando mensageria/produtor de eventos (`EventMessageProducer`). A API persiste o agendamento no banco com sucesso e publica um evento assíncrono para que o serviço de notificação processe em segundo plano, garantindo tempo de resposta baixo para o usuário."

---

### Módulo F: Escalabilidade & Práticas Corporativas (Foco MJV)

#### P9. Se a aplicação precisasse escalar para 100.000 salões e milhões de agendamentos por dia, o que você mudaria?
* **Resposta Ideal:**
  > 1. **Cache Distribuído:** Implementar **Redis** para cache de catálogo de serviços, dados estáticos dos profissionais e configurações de salão.
  > 2. **Particionamento de Banco:** Migrar do modelo de tabela única com coluna discriminadora para **Schema-per-Tenant** ou banco dedicado para clientes de grande porte (Enterprise).
  > 3. **Filas e Assincronismo:** Uso de **RabbitMQ/Kafka** para processamento pesado de relatórios e integração financeira.
  > 4. **Observabilidade:** Instrumentação com **Prometheus, Grafana e OpenTelemetry / Datadog** para monitoramento de APM e rastreamento distribuído."

---

## 4. Dicas Comportamentais & Postura de Tech Lead / Sênior

1. **Fale em termos de Negócio + Tecnologia:** Não diga apenas *"eu usei JPA"*; diga: *"eu usei JPA com EntityGraph para reduzir tempo de resposta do dashboard financeiro para o dono do salão"*.
2. **Demonstre humildade e clareza sobre trade-offs:** Nenhum sistema é perfeito. Se perguntarem sobre pontos de melhoria, diga com segurança: *"A arquitetura atual atende perfeitamente à escala atual com custo baixo. Como evolução técnica planejada, colocaria cache distribuído com Redis e migração do motor de agendamentos para eventos com Kafka."*
3. **Mantenha calma e faça perguntas ao entrevistador:** No final da entrevista, pergunte:
   - *"Qual é a arquitetura predominante nos projetos Java em que atuarei na MJV?"*
   - *"Como o time lida com testes automatizados e esteiras de CI/CD no dia a dia?"*

---
*Documento gerado para suporte em preparação técnica. Boa sorte na entrevista!*
