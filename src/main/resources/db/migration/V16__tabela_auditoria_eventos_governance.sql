-- ===================================================================
-- Flyway Migration V16: Tabela Central de Auditoria Corporativa (Audit Trail)
-- Garante rastreabilidade financeira, operacional e conformidade LGPD
-- ===================================================================

CREATE TABLE IF NOT EXISTS auditoria_evento (
    id BIGSERIAL PRIMARY KEY,
    empresa_id BIGINT,
    usuario_id BIGINT,
    nome_usuario VARCHAR(150),
    ip_origem VARCHAR(45),
    tipo_acao VARCHAR(40) NOT NULL,
    tabela_nome VARCHAR(60),
    registro_id VARCHAR(50),
    detalhes TEXT,
    dados_anteriores TEXT,
    dados_novos TEXT,
    criado_em TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Índices estratégicos para auditoria instantânea por empresa e período
CREATE INDEX IF NOT EXISTS idx_audit_empresa_data ON auditoria_evento (empresa_id, criado_em DESC);
CREATE INDEX IF NOT EXISTS idx_audit_tipo_acao ON auditoria_evento (tipo_acao, criado_em DESC);
CREATE INDEX IF NOT EXISTS idx_audit_tabela_reg ON auditoria_evento (tabela_nome, registro_id);
CREATE INDEX IF NOT EXISTS idx_audit_usuario_data ON auditoria_evento (usuario_id, criado_em DESC);
