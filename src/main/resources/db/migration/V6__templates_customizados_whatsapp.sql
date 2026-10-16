-- ===================================================================
-- Flyway Migration V6: Templates Customizados de WhatsApp
-- ===================================================================

ALTER TABLE whatsapp_configs
    ADD COLUMN IF NOT EXISTS mensagem_comanda_custom VARCHAR(1000),
    ADD COLUMN IF NOT EXISTS mensagem_aniversario_custom VARCHAR(1000);
