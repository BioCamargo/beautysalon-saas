-- ===================================================================
-- Flyway Migration V3: Suporte a Planos Comerciais e Período de Testes (Trial)
-- ===================================================================

-- 1. Colunas de plano e ciclo de assinatura na tabela empresas
ALTER TABLE empresas ADD COLUMN IF NOT EXISTS plano VARCHAR(30) NOT NULL DEFAULT 'STUDIO';
ALTER TABLE empresas ADD COLUMN IF NOT EXISTS status_assinatura VARCHAR(30) NOT NULL DEFAULT 'TRIAL';
ALTER TABLE empresas ADD COLUMN IF NOT EXISTS data_expiracao_trial TIMESTAMP WITHOUT TIME ZONE;
ALTER TABLE empresas ADD COLUMN IF NOT EXISTS data_fim_assinatura TIMESTAMP WITHOUT TIME ZONE;

-- 2. Concede 7 dias de trial a partir da data de criação (ou hoje) para empresas legadas sem data
UPDATE empresas 
SET data_expiracao_trial = COALESCE(criado_em, CURRENT_TIMESTAMP) + INTERVAL '7 days' 
WHERE data_expiracao_trial IS NULL;
