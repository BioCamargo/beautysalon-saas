-- ===================================================================
-- Flyway Migration V7: Chave Pix e Horários de Expediente do Profissional
-- ===================================================================

ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS chave_pix VARCHAR(255);
ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS hora_inicio_expediente TIME DEFAULT '08:00:00';
ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS hora_fim_expediente TIME DEFAULT '19:00:00';
ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS dias_trabalho VARCHAR(100) DEFAULT 'SEGUNDA,TERCA,QUARTA,QUINTA,SEXTA,SABADO';
