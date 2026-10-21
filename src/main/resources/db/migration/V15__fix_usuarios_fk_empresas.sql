-- ===================================================================
-- Flyway Migration V15: Atualiza FK de usuarios para empresas (plural)
-- ===================================================================

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'usuarios_empresa_id_fkey' AND table_name = 'usuarios'
    ) THEN
        ALTER TABLE usuarios DROP CONSTRAINT usuarios_empresa_id_fkey;
    END IF;

    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'empresas') THEN
        ALTER TABLE usuarios 
            ADD CONSTRAINT usuarios_empresa_id_fkey 
            FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE CASCADE;
    END IF;
END $$;
