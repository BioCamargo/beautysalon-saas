-- ===================================================================
-- Flyway Migration V9: Lembrete Inteligente Anti-No-Show de Agendamentos
-- ===================================================================

ALTER TABLE agendamentos
    ADD COLUMN IF NOT EXISTS lembrete_enviado BOOLEAN DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS lembrete_enviado_em TIMESTAMP;

CREATE INDEX IF NOT EXISTS idx_agendamentos_lembrete ON agendamentos (status, lembrete_enviado, data_hora);
