-- ===================================================================
-- Flyway Migration V2: Suporte a Form Builder de Anamnese & Prontuário Clínico
-- ===================================================================

-- 1. Campos adicionais na tabela cliente_anamneses para dados estruturados
ALTER TABLE cliente_anamneses ADD COLUMN IF NOT EXISTS dados_clinicos_json TEXT;
ALTER TABLE cliente_anamneses ADD COLUMN IF NOT EXISTS segmento VARCHAR(50);

-- 2. Tabela de campos customizados para o Form Builder por Empresa/Tenant
CREATE TABLE IF NOT EXISTS anamnese_campos_customizados (
    id BIGSERIAL PRIMARY KEY,
    empresa_id BIGINT NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
    rotulo VARCHAR(150) NOT NULL,
    tipo_campo VARCHAR(30) NOT NULL,
    opcoes VARCHAR(500),
    placeholder VARCHAR(200),
    obrigatorio BOOLEAN NOT NULL DEFAULT FALSE,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    ordem INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_anamnese_campos_empresa_ordem ON anamnese_campos_customizados(empresa_id, ordem);
