-- ===================================================================
-- Flyway Migration V10: Motor de Assinaturas SaaS (DEV-07) & Tracking de Marketing (DEV-08)
-- ===================================================================

-- 1. Campos de Gateway de Pagamento SaaS (Asaas / Stripe)
ALTER TABLE empresas ADD COLUMN IF NOT EXISTS asaas_customer_id VARCHAR(50);
ALTER TABLE empresas ADD COLUMN IF NOT EXISTS asaas_subscription_id VARCHAR(50);
ALTER TABLE empresas ADD COLUMN IF NOT EXISTS link_fatura_atual VARCHAR(255);

-- 2. Campos de Rastreamento de Marketing (Meta Pixel, Google Ads, GA4)
ALTER TABLE empresas ADD COLUMN IF NOT EXISTS meta_pixel_id VARCHAR(50);
ALTER TABLE empresas ADD COLUMN IF NOT EXISTS google_ads_id VARCHAR(50);
ALTER TABLE empresas ADD COLUMN IF NOT EXISTS google_analytics_id VARCHAR(50);
