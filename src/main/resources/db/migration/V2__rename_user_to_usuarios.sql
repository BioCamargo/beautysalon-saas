-- ===================================================================
-- Flyway Migration V2: Rename table "user" to "usuarios"
-- ===================================================================

DO $$
BEGIN
    IF EXISTS (SELECT FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'user') THEN
        ALTER TABLE "user" RENAME TO usuarios;
    END IF;
END $$;
