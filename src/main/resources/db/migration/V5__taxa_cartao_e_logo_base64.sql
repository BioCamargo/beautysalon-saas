-- ===================================================================
-- Flyway Migration V5: Taxa média de cartão para DRE e Logo Base64
-- ===================================================================

-- Adiciona coluna taxa_cartao_estimada para provisionamento no DRE contábil
ALTER TABLE empresas 
    ADD COLUMN IF NOT EXISTS taxa_cartao_estimada NUMERIC(5, 2) DEFAULT 0.00;

-- Amplia o campo logo_url para TEXT permitindo persistência de imagens em Base64 Data URL
ALTER TABLE empresas 
    ALTER COLUMN logo_url TYPE TEXT;
