-- ===================================================================
-- Flyway Migration V14: Saneamento de integridade referencial em caixas legados
-- ===================================================================

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'caixas') THEN
        UPDATE caixas 
        SET operador_abertura_id = NULL 
        WHERE operador_abertura_id IS NOT NULL 
          AND operador_abertura_id NOT IN (SELECT id FROM usuarios);

        UPDATE caixas 
        SET operador_fechamento_id = NULL 
        WHERE operador_fechamento_id IS NOT NULL 
          AND operador_fechamento_id NOT IN (SELECT id FROM usuarios);
    END IF;
END $$;
