-- ===================================================================
-- Flyway Migration V11: Índices Tenant-First & Otimização de Performance DBA
-- NEXI PRO Multi-tenant SaaS (Auditoria 360 - Pilar 1)
-- ===================================================================

-- 1. Busca ultra-rápida de cliente por telefone no Webhook de WhatsApp e CRM
CREATE INDEX IF NOT EXISTS idx_clientes_empresa_telefone 
ON clientes(empresa_id, telefone);

-- 2. Filtro de agendamentos por empresa, status e data para Grade Operacional e Dashboard
CREATE INDEX IF NOT EXISTS idx_agendamentos_empresa_status_data 
ON agendamentos(empresa_id, status, data_hora);

-- 3. Associação de Comandas por Empresa e Caixa Diário para Fechamento Rápido
CREATE INDEX IF NOT EXISTS idx_comandas_empresa_caixa 
ON comandas(empresa_id, caixa_id);
