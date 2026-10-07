-- ===================================================================
-- Flyway Migration V1: Initial Database Schema
-- BeautySalon / LUMORA Multi-tenant SaaS
-- Todas as tabelas no plural e 100% alinhadas com as entidades JPA e AuditableEntity
-- ===================================================================

-- 1. EMPRESAS (Multi-Tenant Root)
CREATE TABLE IF NOT EXISTS empresas (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    cnpj VARCHAR(20),
    slug VARCHAR(100) NOT NULL UNIQUE,
    telefone VARCHAR(30),
    email VARCHAR(150),
    logo_url VARCHAR(500),
    chave_pix VARCHAR(255),
    cidade VARCHAR(100),
    segmento VARCHAR(50) NOT NULL DEFAULT 'SALAO_BELEZA',
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 2. USUARIOS (Autenticação, Perfis e Profissionais)
CREATE TABLE IF NOT EXISTS usuarios (
    id BIGSERIAL PRIMARY KEY,
    empresa_id BIGINT NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
    nome VARCHAR(255),
    username VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL,
    password VARCHAR(255),
    telefone VARCHAR(30),
    especialidade VARCHAR(255),
    cor_agenda VARCHAR(20) DEFAULT '#d4af37',
    image VARCHAR(500),
    tenant_role VARCHAR(50) NOT NULL DEFAULT 'FUNCIONARIO',
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    percentual_comissao NUMERIC(5, 2) DEFAULT 0.00,
    CONSTRAINT uk_usuario_username_empresa UNIQUE (username, empresa_id)
);

-- 3. CLIENTES & CRM (Extends AuditableEntity: created_at, updated_at)
CREATE TABLE IF NOT EXISTS clientes (
    id BIGSERIAL PRIMARY KEY,
    empresa_id BIGINT NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
    nome VARCHAR(255) NOT NULL,
    telefone VARCHAR(30),
    email VARCHAR(150),
    data_nascimento DATE,
    alergias TEXT,
    tipo_cabelo_pele TEXT,
    historico_quimico TEXT,
    observacoes_tecnicas TEXT,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 4. CLIENTE ANAMNESES & EVOLUÇÃO (Extends AuditableEntity: created_at, updated_at)
CREATE TABLE IF NOT EXISTS cliente_anamneses (
    id BIGSERIAL PRIMARY KEY,
    cliente_id BIGINT NOT NULL REFERENCES clientes(id) ON DELETE CASCADE,
    profissional_id BIGINT REFERENCES usuarios(id) ON DELETE SET NULL,
    comanda_id BIGINT,
    procedimento_realizado VARCHAR(255),
    formula_quimica TEXT,
    historico_capilar_alergias TEXT,
    observacoes_tecnicas TEXT,
    foto_antes_url VARCHAR(500),
    foto_depois_url VARCHAR(500),
    assinatura_digital_base64 TEXT,
    termo_consentimento_aceito BOOLEAN NOT NULL DEFAULT TRUE,
    data_registro TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    empresa_id BIGINT NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 5. SERVICOS (Extends AuditableEntity: created_at, updated_at)
CREATE TABLE IF NOT EXISTS servicos (
    id BIGSERIAL PRIMARY KEY,
    empresa_id BIGINT NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
    nome VARCHAR(255) NOT NULL,
    descricao TEXT,
    preco NUMERIC(10, 2),
    imagem VARCHAR(500),
    duracao_minutos INTEGER NOT NULL DEFAULT 30,
    dias_ciclo_retorno INTEGER NOT NULL DEFAULT 30,
    percentual_comissao NUMERIC(5, 2),
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 6. PRODUTOS & ESTOQUE
CREATE TABLE IF NOT EXISTS produtos (
    id BIGSERIAL PRIMARY KEY,
    empresa_id BIGINT NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
    nome VARCHAR(255) NOT NULL,
    codigo_barras VARCHAR(100),
    marca VARCHAR(100),
    categoria VARCHAR(100),
    tipo VARCHAR(50) NOT NULL DEFAULT 'REVENDA',
    preco_custo NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    preco_venda NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    quantidade_estoque INTEGER NOT NULL DEFAULT 0,
    estoque_minimo INTEGER NOT NULL DEFAULT 5,
    unidade_medida VARCHAR(20) DEFAULT 'UN',
    ativo BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS servico_insumos (
    id BIGSERIAL PRIMARY KEY,
    empresa_id BIGINT NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
    servico_id BIGINT NOT NULL REFERENCES servicos(id) ON DELETE CASCADE,
    produto_id BIGINT NOT NULL REFERENCES produtos(id) ON DELETE CASCADE,
    quantidade_gasta INTEGER NOT NULL DEFAULT 1,
    criado_em TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS movimentacoes_estoque (
    id BIGSERIAL PRIMARY KEY,
    empresa_id BIGINT NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
    produto_id BIGINT NOT NULL REFERENCES produtos(id) ON DELETE RESTRICT,
    usuario_id BIGINT REFERENCES usuarios(id) ON DELETE SET NULL,
    tipo VARCHAR(50) NOT NULL,
    quantidade INTEGER NOT NULL,
    saldo_anterior INTEGER,
    saldo_atual INTEGER,
    motivo VARCHAR(255),
    data_hora TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 7. AGENDAMENTOS & AGENDA (Extends AuditableEntity: created_at, updated_at)
CREATE TABLE IF NOT EXISTS agendamentos (
    id BIGSERIAL PRIMARY KEY,
    empresa_id BIGINT NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
    cliente_id BIGINT NOT NULL REFERENCES clientes(id) ON DELETE RESTRICT,
    profissional_id BIGINT REFERENCES usuarios(id) ON DELETE RESTRICT,
    data_hora TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'AGENDADO',
    observacoes TEXT,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS agendamento_servico (
    agendamento_id BIGINT NOT NULL REFERENCES agendamentos(id) ON DELETE CASCADE,
    servico_id BIGINT NOT NULL REFERENCES servicos(id) ON DELETE CASCADE,
    PRIMARY KEY (agendamento_id, servico_id)
);

CREATE TABLE IF NOT EXISTS ausencias_profissionais (
    id BIGSERIAL PRIMARY KEY,
    empresa_id BIGINT NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
    profissional_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    tipo VARCHAR(50) NOT NULL,
    data_especifica DATE,
    dia_semana_recorrente VARCHAR(20),
    hora_inicio TIME,
    hora_fim TIME,
    motivo VARCHAR(255),
    ativo BOOLEAN NOT NULL DEFAULT TRUE
);

-- 8. CAIXAS & FINANCEIRO
CREATE TABLE IF NOT EXISTS caixas (
    id BIGSERIAL PRIMARY KEY,
    empresa_id BIGINT NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
    operador_abertura_id BIGINT REFERENCES usuarios(id) ON DELETE RESTRICT,
    operador_fechamento_id BIGINT REFERENCES usuarios(id) ON DELETE RESTRICT,
    data_abertura TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_fechamento TIMESTAMP WITHOUT TIME ZONE,
    saldo_inicial NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    total_entradas NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    total_saidas NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    saldo_final_esperado NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    saldo_final_contado NUMERIC(10, 2),
    diferenca_fechamento NUMERIC(10, 2),
    status VARCHAR(50) NOT NULL DEFAULT 'ABERTO',
    observacoes TEXT
);

-- COMANDAS (Extends AuditableEntity: created_at, updated_at)
CREATE TABLE IF NOT EXISTS comandas (
    id BIGSERIAL PRIMARY KEY,
    empresa_id BIGINT NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
    cliente_id BIGINT REFERENCES clientes(id) ON DELETE SET NULL,
    cliente_nome_avulso VARCHAR(255),
    caixa_id BIGINT REFERENCES caixas(id) ON DELETE SET NULL,
    agendamento_id BIGINT REFERENCES agendamentos(id) ON DELETE SET NULL,
    numero_comanda VARCHAR(50) NOT NULL,
    data_abertura TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_fechamento TIMESTAMP WITHOUT TIME ZONE,
    subtotal_servicos NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    subtotal_produtos NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    desconto NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    acrescimo NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    valor_total NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    total_comissoes NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    forma_pagamento VARCHAR(50),
    status VARCHAR(50) NOT NULL DEFAULT 'ABERTA',
    cupom_aplicado VARCHAR(50),
    observacoes TEXT,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS comanda_itens (
    id BIGSERIAL PRIMARY KEY,
    comanda_id BIGINT NOT NULL REFERENCES comandas(id) ON DELETE CASCADE,
    servico_id BIGINT REFERENCES servicos(id) ON DELETE SET NULL,
    produto_id BIGINT REFERENCES produtos(id) ON DELETE SET NULL,
    profissional_id BIGINT REFERENCES usuarios(id) ON DELETE SET NULL,
    tipo VARCHAR(50) NOT NULL,
    descricao_item VARCHAR(255),
    quantidade INTEGER NOT NULL DEFAULT 1,
    preco_unitario NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    valor_total NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    percentual_comissao NUMERIC(5, 2) DEFAULT 0.00,
    valor_comissao NUMERIC(10, 2) DEFAULT 0.00
);

-- COMANDA PAGAMENTOS (Extends AuditableEntity: created_at, updated_at)
CREATE TABLE IF NOT EXISTS comanda_pagamentos (
    id BIGSERIAL PRIMARY KEY,
    comanda_id BIGINT NOT NULL REFERENCES comandas(id) ON DELETE CASCADE,
    empresa_id BIGINT NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
    forma_pagamento VARCHAR(50) NOT NULL,
    valor NUMERIC(10, 2) NOT NULL,
    observacao VARCHAR(255),
    data_hora TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS movimentacoes_financeiras (
    id BIGSERIAL PRIMARY KEY,
    empresa_id BIGINT NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
    caixa_id BIGINT NOT NULL REFERENCES caixas(id) ON DELETE CASCADE,
    tipo VARCHAR(50) NOT NULL,
    valor NUMERIC(10, 2) NOT NULL,
    categoria VARCHAR(100),
    descricao VARCHAR(255),
    forma_pagamento VARCHAR(50),
    usuario_id BIGINT REFERENCES usuarios(id) ON DELETE SET NULL,
    data_hora TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 9. FIDELIZAÇÃO & MARKETING
CREATE TABLE IF NOT EXISTS cupons_desconto (
    id BIGSERIAL PRIMARY KEY,
    empresa_id BIGINT NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
    codigo VARCHAR(50) NOT NULL,
    descricao VARCHAR(255),
    tipo_desconto VARCHAR(50) NOT NULL,
    valor_desconto NUMERIC(10, 2) NOT NULL,
    valor_minimo_pedido NUMERIC(10, 2),
    validade_inicio DATE,
    validade_fim DATE,
    limite_usos INTEGER DEFAULT 100,
    usos_atuais INTEGER DEFAULT 0,
    ativo BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS vouchers_presente (
    id BIGSERIAL PRIMARY KEY,
    empresa_id BIGINT NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
    codigo VARCHAR(50) NOT NULL UNIQUE,
    comprador_id BIGINT REFERENCES clientes(id) ON DELETE SET NULL,
    nome_beneficiario VARCHAR(255),
    telefone_beneficiario VARCHAR(30),
    valor_original NUMERIC(10, 2) NOT NULL,
    saldo_restante NUMERIC(10, 2) NOT NULL,
    validade DATE,
    status VARCHAR(50) NOT NULL DEFAULT 'ATIVO',
    mensagem_presente VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS pacotes_combos (
    id BIGSERIAL PRIMARY KEY,
    empresa_id BIGINT NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
    nome VARCHAR(255) NOT NULL,
    descricao TEXT,
    preco_promocional NUMERIC(10, 2) NOT NULL,
    preco_original_soma NUMERIC(10, 2),
    ativo BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS combo_servicos (
    combo_id BIGINT NOT NULL REFERENCES pacotes_combos(id) ON DELETE CASCADE,
    servico_id BIGINT NOT NULL REFERENCES servicos(id) ON DELETE CASCADE,
    PRIMARY KEY (combo_id, servico_id)
);

CREATE TABLE IF NOT EXISTS whatsapp_configs (
    id BIGSERIAL PRIMARY KEY,
    empresa_id BIGINT NOT NULL REFERENCES empresas(id) ON DELETE CASCADE UNIQUE,
    ativo BOOLEAN NOT NULL DEFAULT FALSE,
    provider VARCHAR(50) NOT NULL DEFAULT 'META_OFFICIAL',
    meta_phone_number_id VARCHAR(255),
    meta_access_token VARCHAR(500),
    meta_business_account_id VARCHAR(255),
    instance_name VARCHAR(255),
    api_url VARCHAR(500),
    api_key VARCHAR(500),
    notificar_agendamento BOOLEAN NOT NULL DEFAULT TRUE,
    notificar_lembrete BOOLEAN NOT NULL DEFAULT TRUE,
    notificar_comanda_fechada BOOLEAN NOT NULL DEFAULT TRUE,
    notificar_aniversario BOOLEAN NOT NULL DEFAULT TRUE,
    mensagem_agendamento_custom VARCHAR(1000),
    mensagem_lembrete_custom VARCHAR(1000)
);

-- ===================================================================
-- ÍNDICES PARA ALTA PERFORMANCE (Multi-tenant Queries)
-- ===================================================================
CREATE INDEX IF NOT EXISTS idx_cliente_empresa ON clientes(empresa_id);
CREATE INDEX IF NOT EXISTS idx_servico_empresa ON servicos(empresa_id);
CREATE INDEX IF NOT EXISTS idx_produto_empresa ON produtos(empresa_id);
CREATE INDEX IF NOT EXISTS idx_agendamento_empresa_data ON agendamentos(empresa_id, data_hora);
CREATE INDEX IF NOT EXISTS idx_agendamento_profissional ON agendamentos(profissional_id);
CREATE INDEX IF NOT EXISTS idx_comanda_empresa ON comandas(empresa_id);
CREATE INDEX IF NOT EXISTS idx_comanda_caixa ON comandas(caixa_id);
CREATE INDEX IF NOT EXISTS idx_mov_fin_empresa_data ON movimentacoes_financeiras(empresa_id, data_hora);
