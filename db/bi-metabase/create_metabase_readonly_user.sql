-- ==============================================================================
-- NEXI PRO SaaS — Criação de Usuário Somente-Leitura para o Metabase (BI)
-- Executar conectado como superusuário postgres no banco 'studio'
-- ==============================================================================

-- 1. Cria o usuário com senha forte (Substituir 'MetabaseReadOnlyPass2026!' pela sua senha)
DO
$do$
BEGIN
   IF NOT EXISTS (
      SELECT FROM pg_catalog.pg_roles
      WHERE  rolname = 'metabase_readonly') THEN
      CREATE ROLE metabase_readonly WITH LOGIN PASSWORD 'MetabaseReadOnlyPass2026!';
   END IF;
END
$do$;

-- 2. Concede permissão de conexão ao banco de dados
GRANT CONNECT ON DATABASE studio TO metabase_readonly;

-- 3. Concede uso do schema public
GRANT USAGE ON SCHEMA public TO metabase_readonly;

-- 4. Concede permissão estrita de SELECT em todas as tabelas atuais
GRANT SELECT ON ALL TABLES IN SCHEMA public TO metabase_readonly;

-- 5. Garante que futuras tabelas criadas pelo Flyway também terão SELECT concedido
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT ON TABLES TO metabase_readonly;

-- 6. Revoga qualquer permissão de escrita/alteração por segurança defensiva
REVOKE INSERT, UPDATE, DELETE, TRUNCATE, REFERENCES, TRIGGER ON ALL TABLES IN SCHEMA public FROM metabase_readonly;

-- Mensagem de confirmação
SELECT 'Usuário metabase_readonly configurado com sucesso e isolado em modo somente-leitura!' AS status;
