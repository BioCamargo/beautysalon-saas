# 🧭 Plano Mestre Consolidado: Pendências, Regras & Próximos Passos — NEXI PRO SaaS

> **Classificação:** Documento Oficial de Planejamento da Squad (Engenharia, Produto, UI/UX, DevOps e QA)  
> **Data:** Outubro / 2026 | **Versão:** 2.0 (Pós-Branding NEXI PRO)  
> **Status do Build:** `BUILD SUCCESS` (34/34 Testes Automatizados 100% Aprovados)  
> **Objetivo:** Documento único e unificado que consolida todas as pendências de desenvolvimento, melhorias de UX, regras de negócio e infraestrutura mapeadas em todos os relatórios técnicos do projeto, servindo como guia de execução prioritário a partir de amanhã.

---

## 🏛️ 1. Diagnóstico do Estado Atual (O que já está 100% Pronto)

Antes de listar as pendências, o esquadrão consolidou o que já foi finalizado, validado e comitado:

* ✅ **Branding Oficial NEXI PRO:** Identidade visual completa, ativos 3D oficiais recortados em alta definição (`nexipro-official-logo.png`, `nexipro-icon-official.png` e SVGs responsivos), paleta Electric Cyan (`#00D2EE`), Mint Neon (`#10B981`) e Dark Obsidian (`#0B0E14`). Menção a marcas antigas 100% expurgada.
* ✅ **Multi-Tenant Real & Isolamento Estrito:** Isolamento por empresa via slug na URL (`/{slug}/...`), `TenantInterceptor`, `TenantContext` e migração automática de schema via Flyway (até V9).
* ✅ **Portal de Agendamento Online Público:** Autoatendimento mobile-first em `/{slug}/agendar`, cálculo de horários livres em tempo real, prevenção de conflitos de ausência/almoço e tela de sucesso com atalho para Google Agenda e WhatsApp.
* ✅ **Automação de WhatsApp Oficial (Evolution API & Webhook):**
  - Webhook bidirecional ativo em `/api/v1/webhooks/whatsapp`.
  - Resposta interativa: Cliente responde `1` ➜ status atualiza para `CONFIRMADO`; responde `2` ➜ status atualiza para `CANCELADO` com link de remarcação.
  - Lembrete inteligente anti-no-show disparado **2 horas antes** do atendimento via scheduler automático.
  - Modal interativo de pareamento com **QR Code ao vivo** e auto-configuração de webhook em 1 clique em `/{slug}/configuracoes/whatsapp`.
* ✅ **Frente de Caixa (PDV), Comandas & Estoque:** Abertura e fechamento de caixa, sangrias, reforços, baixa automática de insumos por ficha técnica de procedimentos, e DRE contábil com margem de contribuição.
* ✅ **Anamnese Digital & Ficha Técnica:** Prontuário para estética, fotos antes/depois, histórico químico e assinatura touchscreen.
* ✅ **Segurança & Compatibilidade:** Construtor explícito em `Empresa.java` para compatibilidade total com verificador de bytecode do JDK 25 e Spring Security 6 blindado.

---

## 📊 2. Matriz de Priorização das Pendências (Framework RICE)

As pendências levantadas nos documentos (`LOCAL_BACKLOG_FUTURO_SQUAD_PLG.md`, `ROADMAP_PRODUTO_SAAS_VENDAVEL.md`, `UX_LAWS_ANALISE_E_MELHORIAS_NEXIPRO.md`, `ui_ux_audit_report.md` e `code_review_report.md`) foram organizadas por ordem de execução:

| ID | Iniciativa / Pendência | Tipo | Impacto Principal | Esforço Estimado | Fase de Execução |
| :--- | :--- | :---: | :---: | :---: | :---: |
| **DEV-01** | **Checklist Interativo de Onboarding no Dashboard (PLG)** | Feature / UX | Ativação do Trial (Aha! Moment) | 1 a 2 dias | **Fase 1 (Amanhã)** |
| **DEV-02** | **Botão WhatsApp 1-Clique na Grade de Agendamento** | Feature / Agilidade | Redução de atrito na Recepção | 0.5 dia | **Fase 1 (Amanhã)** |
| **DEV-03** | **Extrato de Comissões Mobile do Profissional (`/meus-ganhos`)** | Feature / Segurança | Retenção da Equipe & Lei do Salão | 1 dia | **Fase 1 (Amanhã)** |
| **DEV-04** | **PWA Mobile-First (Instalação Nativa sem Loja)** | Frontend / Mobile | Experiência de App no Celular | 1 dia | **Fase 2** |
| **DEV-05** | **Filtro de Observabilidade MDC Tenant-Aware em Logs** | Backend / SRE | Diagnóstico e Auditoria em Produção | 0.5 dia | **Fase 2** |
| **DEV-06** | **Pipeline CI/CD (GitHub Actions) & Docker Multi-Stage** | DevOps / Cloud | Automação de deploy e testes | 1 dia | **Fase 2** |
| **DEV-07** | **Billing Recorrente SaaS & Webhook de Pagamentos (Asaas/Stripe)** | Finanças / SaaS | Cobrança automática de mensalidades | 3 a 4 dias | **Fase 3** |
| **DEV-08** | **Meta Pixel & Google Tag nas Configurações da Empresa** | Marketing / Growth | Rastreamento de anúncios do salão | 1 dia | **Fase 3** |

---

## 🎯 3. Especificação Detalhada das Pendências: Fase 1 (Para Executar Amanhã)

### 📌 DEV-01: Checklist Interativo de Onboarding no Dashboard (PLG)
* **Origem:** `docs/LOCAL_BACKLOG_FUTURO_SQUAD_PLG.md` & `docs/UX_LAWS_ANALISE_E_MELHORIAS_NEXIPRO.md` (Zeigarnik Effect).
* **Objetivo:** Conduzir o novo dono de salão ao primeiro atendimento e comanda em menos de 5 minutos, garantindo a conversão do Trial.
* **O que fazer:**
  1. No topo da página `templates/home/indexContent.html` (Dashboard), adicionar um banner elegante e retrátil com design card Obsidian e borda Neon.
  2. Indicador de progresso dinâmico (ex: `2 de 4 concluídos - 50%`):
     - [x] **Passo 1: Salão Cadastrado** (Já concluído no cadastro).
     - [ ] **Passo 2: Cadastrar Serviços** (Link direto para `/{slug}/servicos` com botão "Adicionar Serviço").
     - [ ] **Passo 3: Cadastrar Equipe/Profissionais** (Link direto para `/{slug}/usuarios`).
     - [ ] **Passo 4: Fazer o Primeiro Agendamento** (Link para a grade ou link público `/{slug}/agendar`).
  3. No backend (`HomeController.java`), verificar automaticamente se o tenant possui `servicosCount > 0`, `usuariosCount > 1` e `agendamentosCount > 0` e passar as flags no Model.
  4. Permitir ao usuário clicar em "Ocultar / Já sei usar" (salvo em `localStorage`).
* **Critérios de Aceite:**
  - O percentual atualiza sozinho conforme o dono cadastra serviços ou usuários.
  - Ao atingir 100%, exibe mensagem comemorativa com ícone dourado/neon.

---

### 📌 DEV-02: WhatsApp em 1-Clique na Grade de Agendamento da Recepção
* **Origem:** `docs/LOCAL_BACKLOG_FUTURO_SQUAD_PLG.md` & `docs/UX_LAWS_ANALISE_E_MELHORIAS_NEXIPRO.md` (Fitts's Law).
* **Objetivo:** Permitir que a recepcionista chame a cliente diretamente no WhatsApp com 1 toque na agenda, com mensagem pré-preenchida, para avisar imprevistos ou confirmar horários sem digitar nada.
* **O que fazer:**
  1. Em `templates/agendamentos/list.html` (tanto na tabela quanto nos eventos do FullCalendar):
  2. Adicionar botão flutuante/ícone verde `bi-whatsapp` junto ao nome do cliente.
  3. Gerar link formatado no padrão universal da Meta:
     ```html
     <a th:href="'https://wa.me/55' + ${a.clienteTelefoneLimpo} + '?text=' + ${mensagemWppCodificada}" 
        target="_blank" class="btn btn-sm btn-outline-success">
         <i class="bi bi-whatsapp"></i> Chamar
     </a>
     ```
  4. Texto dinâmico: *"Olá {cliente}! Passando para confirmar seu horário hoje às {hora} para {servico} no {empresa}."*
* **Critérios de Aceite:**
  - Ao clicar no computador, abre o WhatsApp Web em nova aba com o número e o texto já preenchidos.
  - Ao clicar no celular/tablet, abre o app nativo do WhatsApp.

---

### 📌 DEV-03: Extrato de Comissões e Ganhos Exclusivo do Profissional (`/meus-ganhos`)
* **Origem:** `docs/LOCAL_BACKLOG_FUTURO_SQUAD_PLG.md` & `docs/CHECKLIST_VALIDACAO_NEXIPRO_VIP.md` (Módulo 14).
* **Objetivo:** O profissional parceiro (cabeleireiro, manicure, barbeiro) pode consultar seus próprios atendimentos e comissão acumulada pelo celular, sem ver o faturamento global do salão.
* **O que fazer:**
  1. Criar controller `MeusGanhosController.java` (`GET /{slug}/meus-ganhos`).
  2. Restrição de segurança: Permite acesso para usuários com `tenantRole = FUNCIONARIO` ou `RECEPCAO` (além de ADMIN/OWNER).
  3. Filtrar comanda_itens estritamente pelo `user_id` autenticado na sessão.
  4. Exibir cards resumidos:
     - **Ganhos de Hoje (R$)**
     - **Ganhos da Semana / Quinzena (R$)**
     - **Lista detalhada de procedimentos realizados:** Data, cliente, serviço, valor do serviço e valor líquido da comissão do profissional.
  5. Criar template moderno mobile-first `templates/profissional/meus-ganhos.html`.
* **Critérios de Aceite:**
  - O profissional A jamais visualiza os dados do profissional B nem o lucro do estabelecimento.
  - Cálculo de centavos idêntico ao rateio da comanda fechada.

---

## ⚡ 4. Especificação Detalhada: Fase 2 (Próximos Passos Imediatos)

### 📌 DEV-04: PWA Mobile-First (Progressive Web App Oficial NEXI PRO)
* **Origem:** `docs/LOCAL_BACKLOG_FUTURO_SQUAD_PLG.md` (Iniciativa 6).
* **O que fazer:**
  1. Criar `src/main/resources/static/manifest.json`:
     ```json
     {
       "name": "NEXI PRO - Gestão Inteligente",
       "short_name": "NEXI PRO",
       "start_url": "/",
       "display": "standalone",
       "background_color": "#0B0E14",
       "theme_color": "#00D2EE",
       "icons": [
         {
           "src": "/images/nexipro-icon-official.png",
           "sizes": "192x192",
           "type": "image/png"
         },
         {
           "src": "/images/nexipro-icon-official.png",
           "sizes": "512x512",
           "type": "image/png"
         }
       ]
     }
     ```
  2. Criar `service-worker.js` para cache estático e funcionamento offline parcial.
  3. Vincular em `layout.html` e `agendamento.html` com meta tags para iOS (`apple-mobile-web-app-capable`).
  4. Adicionar prompt visual convidativo de instalação na tela de autoatendimento.

---

### 📌 DEV-05: Observabilidade Tenant-Aware em Logs (MDC Filter)
* **Origem:** `docs/LOCAL_BACKLOG_FUTURO_SQUAD_PLG.md` & `docs/code_review_report.md`.
* **O que fazer:**
  1. Criar `TenantLoggingFilter.java` interceptando todas as requisições HTTP.
  2. Injetar no `org.slf4j.MDC`:
     - `tenant = TenantContext.getSlug()`
     - `empresa_id = TenantContext.getEmpresaId()`
     - `usuario = SecurityContextHolder.getContext().getAuthentication().getName()`
  3. Bloco `try-finally` com `MDC.clear()` obrigatório para não contaminar threads reutilizadas do Tomcat.
  4. Ajustar formato de log em `application.properties`:
     ```properties
     logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} [tenant=%X{tenant}] [user=%X{usuario}] - %msg%n
     ```

---

### 📌 DEV-06: CI/CD no GitHub Actions & Dockerfile Multi-Stage
* **Origem:** `docs/LOCAL_BACKLOG_FUTURO_SQUAD_PLG.md` (Iniciativa 7).
* **O que fazer:**
  1. Criar `.github/workflows/ci.yml`:
     - Disparar em `push` e `pull_request` para `main` e `feat/*`.
     - Rodar `mvnw clean test` em container Linux com JDK 17/21.
  2. Criar `Dockerfile` multi-stage:
     - Stage 1 (Build): `maven:3.9-eclipse-temurin-17-alpine`
     - Stage 2 (Runtime): `eclipse-temurin:17-jre-alpine` com usuário não-root `spring`.
     - Redução do tamanho da imagem de ~600MB para menos de **180MB**.

---

## 💳 5. Especificação Detalhada: Fase 3 (Monetização & Escala de Negócio)

### 📌 DEV-07: Motor de Assinaturas SaaS (Billing & Webhooks Asaas/Stripe)
* **Origem:** `docs/ROADMAP_PRODUTO_SAAS_VENDAVEL.md` (Pilar 1) & `docs/LOCAL_BACKLOG_FUTURO_SQUAD_PLG.md`.
* **O que fazer:**
  1. Integração com gateway nacional (**Asaas** ou **Mercado Pago**):
     - Suporte a Cartão de Crédito e Pix Recorrente.
     - Criação de cliente e assinatura automática vinculada ao `Empresa.id`.
  2. Webhook `POST /api/webhooks/billing`:
     - Evento `PAYMENT_CONFIRMED`: Estende `data_fim_assinatura` em +30 dias e define `statusAssinatura = ATIVO`.
     - Evento `PAYMENT_OVERDUE`: Define status como `INADIMPLENTE` com aviso amigável no topo do sistema.
  3. Interceptor de Bloqueio Suave:
     - Contas vencidas há mais de 3 dias entram em modo "Somente Leitura" (bloqueia gravação de novos agendamentos e comandas até a regularização).

---

### 📌 DEV-08: Rastreamento com Meta Pixel & Google Ads para Salões
* **Origem:** `docs/LOCAL_MELHORIAS_MARKETING_PROPAGANDA_DIVULGACAO_STUDIO.md` (Pilar 1).
* **O que fazer:**
  1. Adicionar campos `metaPixelId` e `googleAdsId` na entidade `Empresa` e na tela `configuracoes/empresa.html`.
  2. No portal público de agendamento (`agendamento.html` e `sucesso.html`), injetar o script do pixel quando preenchido.
  3. Disparar eventos padrão da Meta:
     - `PageView` na vitrine de serviços.
     - `InitiateCheckout` na seleção de horário.
     - `Schedule` na conclusão com sucesso (permitindo ao salão calcular o ROI real de anúncios no Instagram).

---

## 📋 6. Checklist de Execução para Retomada Amanhã

Quando iniciarmos os trabalhos amanhã, o roteiro de execução será exatamente este:

- [ ] **Passo 1:** Executar `DEV-01` (Checklist dinâmico de Onboarding no topo do Dashboard).
- [ ] **Passo 2:** Executar `DEV-02` (Botão de WhatsApp rápido na grade de agendamentos).
- [ ] **Passo 3:** Executar `DEV-03` (Tela de Extrato de Ganhos e Comissões do Profissional `/{slug}/meus-ganhos`).
- [ ] **Passo 4:** Rodar a suíte completa de testes automatizados (`mvn test`) para assegurar 100% green.
- [ ] **Passo 5:** Fazer commit semântico em bloco e push ao GitHub.
- [ ] **Passo 6:** Iniciar a Fase 2 (PWA Mobile e Observabilidade MDC).

---

> 💡 **Nota da Squad:** Este documento unifica e substitui a necessidade de checar múltiplos arquivos avulsos. Todos os apontamentos foram validados contra a base de código ativa e estão prontos para desenvolvimento imediato.
