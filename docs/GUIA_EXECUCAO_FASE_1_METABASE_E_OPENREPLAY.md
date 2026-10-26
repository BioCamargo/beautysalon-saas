# 🚀 NEXI PRO — Guia de Execução: Fase 1 (Metabase & OpenReplay)

> **Status:** Entregue & Pronto para Operação  
> **Componentes:** OpenReplay (Observabilidade de UX e Sessões) & Metabase (Business Intelligence do Fundador)  

---

## 🎥 1. OpenReplay: Gravação de Sessões & Auditoria de UX no Trial

O **OpenReplay** foi integrado de forma modular e segura via [fragmento Thymeleaf](file:///c:/Dev/Projetos/Proj_Studio/src/main/resources/templates/fragments/analytics.html), presente no layout principal (`layout.html`) e na tela de registro (`register.html`).

### 🛡️ Conformidade LGPD & Mascaramento Automático
* **Default Input Mode = 1:** Todos os campos de digitação (nomes, CPFs, senhas, observações de caixa e anamnese) são **mascarados por padrão**. Nenhuma informação sensível é gravada em vídeo.
* **Metadados Multi-Tenant Injetados:**
  * `empresa_slug`: Permite filtrar gravações por empresa específica.
  * `trial_ativo`: Permite ao fundador filtrar e assistir apenas as sessões de usuários que estão testando nos 7 dias de degustação VIP.
  * `plano`: Permite segmentar sessões por Plano Solo, Studio ou VIP.

### ⚙️ Como Ativar em Produção ou Localmente
Basta definir as duas variáveis de ambiente no Render ou no seu arquivo `.env`:
```bash
OPENREPLAY_ENABLED=true
OPENREPLAY_PROJECT_KEY=seu_project_key_do_openreplay
```
*(Se `OPENREPLAY_ENABLED` for `false` ou a chave estiver vazia, o script não é renderizado, garantindo zero impacto de performance quando desativado).*

---

## 📊 2. Metabase: BI & Painel Executivo do Fundador

O Metabase permite ao fundador acompanhar as métricas vitais do SaaS (MRR, Churn, Conversão de Trial, GMV transacionado) sem precisar programar relatórios no frontend.

### 🐳 Passo 1: Subir o Container do Metabase
No terminal, execute o Docker Compose configurado:
```powershell
docker compose -f docker-compose-metabase.yml up -d
```
O painel estará disponível em: **`http://localhost:3000`**

*(O container já possui limite de memória configurado em `-Xmx512m` para respeitar as diretrizes de FinOps sem sobrecarregar a máquina).*

---

### 🔒 Passo 2: Criar o Usuário Read-Only no PostgreSQL
Para garantir que o Metabase nunca altere dados acidentalmente, execute o script SQL de permissões limitadas:
* Arquivo: [**`db/bi-metabase/create_metabase_readonly_user.sql`**](file:///c:/Dev/Projetos/Proj_Studio/db/bi-metabase/create_metabase_readonly_user.sql)

```sql
-- Cria o usuário metabase_readonly com permissão apenas de SELECT
DO $do$ BEGIN
   IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'metabase_readonly') THEN
      CREATE ROLE metabase_readonly WITH LOGIN PASSWORD 'MetabaseReadOnlyPass2026!';
   END IF;
END $do$;

GRANT CONNECT ON DATABASE studio TO metabase_readonly;
GRANT USAGE ON SCHEMA public TO metabase_readonly;
GRANT SELECT ON ALL TABLES IN SCHEMA public TO metabase_readonly;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT ON TABLES TO metabase_readonly;
```

---

### 🔌 Passo 3: Conectar o Metabase ao Banco de Dados
Ao abrir `http://localhost:3000` pela primeira vez:
1. Crie sua conta de administrador do Metabase.
2. Na tela de conexão com banco de dados, selecione **PostgreSQL**:
   * **Host:** `postgres` (se rodando na mesma rede Docker) ou `localhost` (ou a URL externa do Render).
   * **Port:** `5432`
   * **Database name:** `studio`
   * **Database username:** `metabase_readonly`
   * **Database password:** `MetabaseReadOnlyPass2026!`

---

### 📈 Passo 4: Criar as Consultas Prontas do Dashboard
Utilize o catálogo de consultas SQL preparadas em [**`db/bi-metabase/queries_dashboard_fundador.sql`**](file:///c:/Dev/Projetos/Proj_Studio/db/bi-metabase/queries_dashboard_fundador.sql):

1. **MRR Atual e Faturamento por Plano:** Gráfico de Rosca / Número único com o faturamento recorrente mensal estimado.
2. **Funil de Conversão do Trial (Últimos 30 dias):** Gráfico de Barras mostrando percentual de conversão de cadastros em planos ativos.
3. **Volume Transacionado em Salões (GMV):** Gráfico de pizza com split de Pix, Cartão de Crédito/Débito e Dinheiro.
4. **Top 10 Procedimentos Mais Populares:** Tabela ranqueada por procedimentos mais realizados e volume de comissões geradas.
5. **Crescimento Histórico Mensal:** Gráfico de linhas com novas empresas cadastradas mês a mês.
