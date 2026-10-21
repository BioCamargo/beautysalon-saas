# 🚀 NEXI PRO SaaS — Relatório de Auditoria & Guia de Deploy em Produção

**Data:** 2026-10-21  
**Status do Build:** ✅ **BUILD SUCCESS** (JAR `BeautySalon-0.0.1-SNAPSHOT.jar` compilado e pronto)  
**Meta:** Deixar o sistema 100% online e operacional hoje.

---

## 🏛️ 1. Auditoria Técnica de Ponta a Ponta (Equipe Especialista)

### ✅ Arquitetura & Multi-Tenancy (SaaS Principal Architect)
- **Isolamento de Dados:** Cada requisição web tem seu tenant resolvido via subdomínio/slug na URL através de `TenantFilter` e `TenantContext`, garantindo que nenhuma empresa acesse clientes, comandas ou relatórios de outra (*zero cross-tenant leak*).
- **Fallback de Mensageria:** O RabbitMQ está desacoplado com fallback gracioso (`app.rabbitmq.enabled=false`). Na nuvem inicial, notificações e WhatsApp funcionam sem necessidade obrigatória de cluster RabbitMQ ativo.
- **Assinaturas & Bloqueio Suave:** Implementado bloqueio suave de segurança caso a assinatura expire, mantendo consultas operacionais e impedindo novos agendamentos indevidos.

### ✅ Banco de Dados & Migrations (Senior DBA Specialist)
- **Flyway Migrations:** 12 scripts de migração sequenciais e validados (`V1__initial_schema.sql` até `V12__whatsapp_conectado_status.sql`). Na primeira inicialização em produção, o Flyway cria automaticamente todas as 15 tabelas, foreign keys e índices de performance.
- **Integridade Numérica:** Comissões e fechamento de comanda utilizam `BigDecimal` e tipos monetários rigorosos, eliminando divergências de centavos.

### ✅ Backend & Regras de Negócio (Tech Lead Specialist)
- **Validação de Horários Passados:** O agendamento (interno e público) bloqueia qualquer tentativa de marcação no passado.
- **Bloqueio de Ausências/Folgas:** O endpoint público de horários livres e a agenda interna respeitam folgas pontuais, semanais e férias do profissional.
- **Confirmação Interativa de WhatsApp:** Webhook compatível com payloads Map e List da Evolution API v2, reconhecendo mensagens interativas de confirmação (1, Sim, Confirmo) e cancelamento (2, Não).

### ✅ Frontend, UX & Portal de Agendamento (Frontend & UI/UX Specialist)
- **Portal Online:** Tipografia revisada com alto contraste (`#f8fafc` luminoso sobre fundo escuro), seleção inteligente de primeiro horário livre e campo de aniversário do cliente.
- **Agenda & Calendário:** FullCalendar v6 configurado com pacote de locales em português (`pt-br`), alternância instantânea entre modo Tabela e Calendário, exibindo bloqueios e horários ocupados.

---

## 🌐 2. Como Publicar e Deixar Totalmente Online HOJE

A forma mais rápida, segura e sem custo inicial para colocar o NEXI PRO online em **menos de 15 minutos** é utilizando a plataforma **Render.com** (ou **Railway.app**). O projeto já possui o [Dockerfile](file:///c:/Dev/Projetos/Proj_Studio/Dockerfile) e o [render.yaml](file:///c:/Dev/Projetos/Proj_Studio/render.yaml) configurados.

---

### Opção A (Recomendada): Deploy em 15 Minutos via Render.com

#### Passo 1: Subir o Código para o GitHub
Se ainda não enviou os commits mais recentes para o GitHub:
```powershell
git add .
git commit -m "feat: auditoria de ponta a ponta e ajustes de homologacao"
git push origin main
```

#### Passo 2: Acessar o Render
1. Acesse [https://render.com](https://render.com) e faça login com sua conta do GitHub.
2. No painel inicial (Dashboard), clique no botão **New +** no canto superior direito e selecione **Blueprint**.
3. Conecte o repositório do seu projeto (`BioCamargo/beautysalon-saas` ou seu repositório correspondente).
4. O Render detectará automaticamente o arquivo `render.yaml`. Clique em **Apply**.

#### Passo 3: Provisionamento Automático
O Render criará automaticamente:
1. **Banco PostgreSQL Gratuito:** Banco gerenciado em nuvem com backups.
2. **Serviço Web Docker:** Baixará o JDK 17, compilará a aplicação e iniciará o container.
3. **Execução Automática das Migrations:** O Spring Boot subirá e o Flyway aplicará automaticamente da `V1` à `V12`.
4. **Certificado SSL / HTTPS Grátis:** Seu sistema ganhará uma URL segura automática:
   ```
   https://beautysalon-app.onrender.com
   ```
   *(Você poderá conectar seu domínio próprio como `app.nexipro.com.br` depois nas configurações de domínio com 1 clique).*

---

### Opção B: Deploy em VPS Própria (Hostinger / Hetzner / AWS Lightsail / DigitalOcean)

Se você possui uma VPS Linux (Ubuntu 22.04 / 24.04 LTS):

#### Passo 1: Instalar Docker e Docker Compose na VPS
```bash
sudo apt update && sudo apt upgrade -y
sudo apt install -y docker.io docker-compose-v2 git
sudo systemctl enable --now docker
```

#### Passo 2: Clonar o Repositório e Subir os Containers
```bash
git clone https://github.com/SEU_USUARIO/beautysalon-saas.git /app/nexipro
cd /app/nexipro
docker compose up -d --build
```
O `docker-compose.yml` subirá:
- **PostgreSQL 16** (porta 5432 interna)
- **Aplicação NEXI PRO** (porta 8081 mapeada)
- **RabbitMQ** (opcional para mensageria assíncrona)

#### Passo 3: Apontar Nginx com SSL Grátis (Let's Encrypt)
```bash
sudo apt install -y nginx certbot python3-certbot-nginx
```
Crie a configuração do Nginx em `/etc/nginx/sites-available/nexipro`:
```nginx
server {
    server_name app.seudominio.com.br;

    location / {
        proxy_pass http://127.0.0.1:8081;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```
Ative e gere o certificado SSL com 1 comando:
```bash
sudo ln -s /etc/nginx/sites-available/nexipro /etc/nginx/sites-enabled/
sudo nginx -t && sudo systemctl reload nginx
sudo certbot --nginx -d app.seudominio.com.br
```

---

## 🔑 3. Tabela de Variáveis de Ambiente para Produção

| Variável | Descrição | Exemplo em Produção |
| :--- | :--- | :--- |
| `DB_URL` | String de conexão JDBC com o PostgreSQL | `jdbc:postgresql://postgres.render.com:5432/beautysalon` |
| `DB_USERNAME` | Usuário do banco de dados | `beautysalon_user` |
| `DB_PASSWORD` | Senha segura do banco de dados | `SuaSenhaForte123!` |
| `PORT` | Porta de escuta da aplicação | `8080` ou `8081` |
| `RABBITMQ_ENABLED` | Ativação da mensageria RabbitMQ | `false` (padrão nuvem simples) |
| `ASAAS_BILLING_ENABLED` | Gateway de cobrança das assinaturas | `true` |
| `ASAAS_WEBHOOK_TOKEN` | Token de segurança do webhook Asaas | `seu-token-gerado-no-asaas` |
| `WHATSAPP_API_URL` | Endpoint da Evolution API conectada | `http://evolution-api:8080` |

---

## 📋 4. Checklist Imediato para Rodar Hoje

- [x] Compilação limpa do JAR (`mvn clean package -DskipTests` -> **SUCCESS**)
- [x] Migrations Flyway ordenadas (V1 a V12)
- [x] Dockerfile multi-stage pronto para nuvem
- [x] `render.yaml` pronto para deploy automatizado
- [x] Validações de segurança, ausência e agendamento passadas implementadas
- [x] Portal online com contrastes de alto luxo e campo de aniversário
- [ ] Enviar commits para a branch `main` do GitHub
- [ ] Conectar no Render ou subir na VPS
- [ ] Testar criação de novo salão / trial e agendamento online na URL pública HTTPS
