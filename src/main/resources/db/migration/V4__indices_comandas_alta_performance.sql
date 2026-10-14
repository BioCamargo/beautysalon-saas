-- ===================================================================
-- Flyway Migration V4: Índices de Alta Performance para Consultas Financeiras
-- BeautySalon / LUMORA Multi-tenant SaaS
-- ===================================================================

-- 1. Índice Composto para consultas de Comandas por Período e Status (Dashboard e DRE)
CREATE INDEX IF NOT EXISTS idx_comandas_empresa_status_fechamento 
ON comandas(empresa_id, status, data_fechamento);

-- 2. Índice Composto para agilizar carregamento de itens de comanda filtrados por tipo
CREATE INDEX IF NOT EXISTS idx_comanda_itens_comanda_tipo 
ON comanda_itens(comanda_id, tipo);

-- 3. Índice em servico_insumos para acelerar agrupamento por empresa
CREATE INDEX IF NOT EXISTS idx_servico_insumos_empresa 
ON servico_insumos(empresa_id);
