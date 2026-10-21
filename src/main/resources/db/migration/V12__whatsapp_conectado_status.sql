-- ===================================================================
-- Flyway Migration V12: Status de Conexão WhatsApp Plug-and-Play
-- Permite controle visual direto de pareamento e número conectado
-- ===================================================================

ALTER TABLE whatsapp_configs
    ADD COLUMN IF NOT EXISTS conectado BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS numero_conectado VARCHAR(50);
