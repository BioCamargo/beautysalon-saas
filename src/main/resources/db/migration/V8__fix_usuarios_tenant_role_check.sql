-- ===================================================================
-- Flyway Migration V8: Atualizar constraint de TenantRole em usuarios
-- Permite os novos papéis RECEPCAO e PROFISSIONAL
-- ===================================================================

ALTER TABLE usuarios DROP CONSTRAINT IF EXISTS usuarios_tenant_role_check;

ALTER TABLE usuarios ADD CONSTRAINT usuarios_tenant_role_check 
    CHECK (tenant_role IN ('OWNER', 'ADMIN', 'RECEPCAO', 'PROFISSIONAL', 'FUNCIONARIO'));
