-- ===================================================================
-- Flyway Migration V1: Initial Database Schema
-- BeautySalon / LUMORA Multi-tenant SaaS
-- ===================================================================

-- 1. EMPRESA (Multi-Tenant Root)
CREATE TABLE IF NOT EXISTS empresa (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    cnpj VARCHAR(20) UNIQUE,
    slug VARCHAR(100) NOT NULL UNIQUE,
    telefone VARCHAR(30),
    email VARCHAR(150),
    endereco VARCHAR(255),
    logo_url VARCHAR(500),
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 2. USUARIOS (Autenticação, Perfis e Profissionais)
CREATE TABLE IF NOT EXISTS usuarios (
    id BIGSERIAL PRIMARY KEY,
    empresa_id BIGINT REFERENCES empresa(id) ON DELETE CASCADE,
    nome VARCHAR(255) NOT NULL,
    username VARCHAR(100),
    email VARCHAR(150) NOT NULL,
    password VARCHAR(255) NOT NULL,
    telefone VARCHAR(30),
    especialidade VARCHAR(255),
    cor_agenda VARCHAR(20) DEFAULT '#d4af37',
    image VARCHAR(500),
    tenant_role VARCHAR(50) NOT NULL DEFAULT 'FUNCIONARIO',
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    percentual_comissao NUMERIC(5, 2) DEFAULT 0.00,
    criado_em TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_usuario_username_empresa UNIQUE (username, empresa_id)
);

-- 3. CLIENTE & CRM
CREATE TABLE IF NOT EXISTS cliente (
    id BIGSERIAL PRIMARY KEY,
    empresa_id BIGINT NOT NULL REFERENCES empresa(id) ON DELETE CASCADE,
    nome VARCHAR(255) NOT NULL,
    telefone VARCHAR(30),
    email VARCHAR(150),
    cpf VARCHAR(20),
    data_nascimento DATE,
    observacoes TEXT,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS cliente_anamnese (
    id BIGSERIAL PRIMARY KEY,
    cliente_id BIGINT NOT NULL REFERENCES cliente(id) ON DELETE CASCADE,
    alergias TEXT,
    tipo_pele_cabelo VARCHAR(255),
    restricoes_medicas TEXT,
    historico_procedimentos TEXT,
    observacoes TEXT,
    criado_em TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 4. SERVICO (Catálogo de Serviços)
CREATE TABLE IF NOT EXISTS servico (
    id BIGSERIAL PRIMARY KEY,
    empresa_id BIGINT NOT NULL REFERENCES empresa(id) ON DELETE CASCADE,
    nome VARCHAR(255) NOT NULL,
    descricao TEXT,
    preco NUMERIC(10, 2) NOT NULL,
    duracao_minutos INTEGER NOT NULL DEFAULT 30,
    comissao_percentual NUMERIC(5, 2) DEFAULT 0.00,
    categoria VARCHAR(100),
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 5. PRODUTO & ESTOQUE
CREATE TABLE IF NOT EXISTS produto (
    id BIGSERIAL PRIMARY KEY,
    empresa_id BIGINT NOT NULL REFERENCES empresa(id) ON DELETE CASCADE,
    nome VARCHAR(255) NOT NULL,
    codigo_barras VARCHAR(100),
    tipo_produto VARCHAR(50) NOT NULL DEFAULT 'REVENDA',
    preco_custo NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    preco_venda NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    quantidade_estoque INTEGER NOT NULL DEFAULT 0,
    estoque_minimo INTEGER NOT NULL DEFAULT 5,
    unidade_medida VARCHAR(20) DEFAULT 'UN',
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS servico_insumo (
    id BIGSERIAL PRIMARY KEY,
    servico_id BIGINT NOT NULL REFERENCES servico(id) ON DELETE CASCADE,
    produto_id BIGINT NOT NULL REFERENCES produto(id) ON DELETE CASCADE,
    quantidade_gasta NUMERIC(10, 3) NOT NULL
);

CREATE TABLE IF NOT EXISTS movimentacao_estoque (
    id BIGSERIAL PRIMARY KEY,
    empresa_id BIGINT NOT NULL REFERENCES empresa(id) ON DELETE CASCADE,
    produto_id BIGINT NOT NULL REFERENCES produto(id) ON DELETE RESTRICT,
    usuario_id BIGINT REFERENCES usuarios(id) ON DELETE SET NULL,
    tipo VARCHAR(50) NOT NULL,
    quantidade INTEGER NOT NULL,
    motivo VARCHAR(255),
    data_movimentacao TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 6. AGENDAMENTO & AGENDA
CREATE TABLE IF NOT EXISTS agendamento (
    id BIGSERIAL PRIMARY KEY,
    empresa_id BIGINT NOT NULL REFERENCES empresa(id) ON DELETE CASCADE,
    cliente_id BIGINT NOT NULL REFERENCES cliente(id) ON DELETE RESTRICT,
    profissional_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE RESTRICT,
    servico_id BIGINT NOT NULL REFERENCES servico(id) ON DELETE RESTRICT,
    data_hora_inicio TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    data_hora_fim TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'AGENDADO',
    valor NUMERIC(10, 2) NOT NULL,
    observacao TEXT,
    criado_em TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS ausencia_profissional (
    id BIGSERIAL PRIMARY KEY,
    empresa_id BIGINT NOT NULL REFERENCES empresa(id) ON DELETE CASCADE,
    profissional_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    data_inicio TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    data_fim TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    motivo VARCHAR(255)
);

-- 7. CAIXA & FINANCEIRO
CREATE TABLE IF NOT EXISTS caixa (
    id BIGSERIAL PRIMARY KEY,
    empresa_id BIGINT NOT NULL REFERENCES empresa(id) ON DELETE CASCADE,
    usuario_abertura_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE RESTRICT,
    usuario_fechamento_id BIGINT REFERENCES usuarios(id) ON DELETE RESTRICT,
    data_abertura TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_fechamento TIMESTAMP WITHOUT TIME ZONE,
    saldo_inicial NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    saldo_final_calculado NUMERIC(10, 2) DEFAULT 0.00,
    saldo_final_informado NUMERIC(10, 2) DEFAULT 0.00,
    diferenca NUMERIC(10, 2) DEFAULT 0.00,
    status VARCHAR(50) NOT NULL DEFAULT 'ABERTO',
    observacao TEXT
);

CREATE TABLE IF NOT EXISTS comanda (
    id BIGSERIAL PRIMARY KEY,
    empresa_id BIGINT NOT NULL REFERENCES empresa(id) ON DELETE CASCADE,
    cliente_id BIGINT REFERENCES cliente(id) ON DELETE SET NULL,
    caixa_id BIGINT REFERENCES caixa(id) ON DELETE SET NULL,
    numero_comanda VARCHAR(50),
    data_abertura TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_fechamento TIMESTAMP WITHOUT TIME ZONE,
    status VARCHAR(50) NOT NULL DEFAULT 'ABERTA',
    valor_bruto NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    valor_desconto NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    valor_pago NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    valor_total NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    observacao TEXT
);

CREATE TABLE IF NOT EXISTS comanda_item (
    id BIGSERIAL PRIMARY KEY,
    comanda_id BIGINT NOT NULL REFERENCES comanda(id) ON DELETE CASCADE,
    servico_id BIGINT REFERENCES servico(id) ON DELETE SET NULL,
    produto_id BIGINT REFERENCES produto(id) ON DELETE SET NULL,
    profissional_id BIGINT REFERENCES usuarios(id) ON DELETE SET NULL,
    quantidade INTEGER NOT NULL DEFAULT 1,
    valor_unitario NUMERIC(10, 2) NOT NULL,
    valor_desconto NUMERIC(10, 2) DEFAULT 0.00,
    valor_total NUMERIC(10, 2) NOT NULL,
    valor_comissao NUMERIC(10, 2) DEFAULT 0.00
);

CREATE TABLE IF NOT EXISTS pagamento_comanda (
    id BIGSERIAL PRIMARY KEY,
    comanda_id BIGINT NOT NULL REFERENCES comanda(id) ON DELETE CASCADE,
    caixa_id BIGINT REFERENCES caixa(id) ON DELETE SET NULL,
    forma_pagamento VARCHAR(50) NOT NULL,
    valor NUMERIC(10, 2) NOT NULL,
    data_pagamento TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS movimentacao_financeira (
    id BIGSERIAL PRIMARY KEY,
    empresa_id BIGINT NOT NULL REFERENCES empresa(id) ON DELETE CASCADE,
    caixa_id BIGINT REFERENCES caixa(id) ON DELETE SET NULL,
    tipo VARCHAR(50) NOT NULL,
    categoria VARCHAR(100),
    descricao VARCHAR(255) NOT NULL,
    valor NUMERIC(10, 2) NOT NULL,
    data_movimentacao TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    forma_pagamento VARCHAR(50)
);

-- 8. FIDELIZAÇÃO & MARKETING
CREATE TABLE IF NOT EXISTS cupom_desconto (
    id BIGSERIAL PRIMARY KEY,
    empresa_id BIGINT NOT NULL REFERENCES empresa(id) ON DELETE CASCADE,
    codigo VARCHAR(50) NOT NULL,
    tipo_desconto VARCHAR(50) NOT NULL,
    valor_desconto NUMERIC(10, 2) NOT NULL,
    valor_minimo_comanda NUMERIC(10, 2) DEFAULT 0.00,
    data_validade DATE,
    limite_usos INTEGER DEFAULT 0,
    vezes_usado INTEGER DEFAULT 0,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS voucher_presente (
    id BIGSERIAL PRIMARY KEY,
    empresa_id BIGINT NOT NULL REFERENCES empresa(id) ON DELETE CASCADE,
    codigo VARCHAR(50) NOT NULL UNIQUE,
    cliente_comprador_id BIGINT REFERENCES cliente(id) ON DELETE SET NULL,
    nome_beneficiario VARCHAR(255) NOT NULL,
    telefone_beneficiario VARCHAR(30),
    valor_inicial NUMERIC(10, 2) NOT NULL,
    saldo_restante NUMERIC(10, 2) NOT NULL,
    data_validade DATE NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'ATIVO',
    criado_em TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS pacote_combo (
    id BIGSERIAL PRIMARY KEY,
    empresa_id BIGINT NOT NULL REFERENCES empresa(id) ON DELETE CASCADE,
    nome VARCHAR(255) NOT NULL,
    descricao TEXT,
    preco_promocional NUMERIC(10, 2) NOT NULL,
    validade_dias INTEGER DEFAULT 30,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS whats_app_config (
    id BIGSERIAL PRIMARY KEY,
    empresa_id BIGINT NOT NULL REFERENCES empresa(id) ON DELETE CASCADE,
    api_url VARCHAR(500),
    api_token VARCHAR(500),
    numero_whatsapp VARCHAR(30),
    enviar_confirmacao_agendamento BOOLEAN NOT NULL DEFAULT TRUE,
    enviar_lembrete_24h BOOLEAN NOT NULL DEFAULT TRUE,
    enviar_lembrete_2h BOOLEAN NOT NULL DEFAULT TRUE,
    mensagem_lembrete TEXT
);

-- ===================================================================
-- ÍNDICES PARA ALTA PERFORMANCE (Multi-tenant Queries)
-- ===================================================================
CREATE INDEX IF NOT EXISTS idx_cliente_empresa ON cliente(empresa_id);
CREATE INDEX IF NOT EXISTS idx_servico_empresa ON servico(empresa_id);
CREATE INDEX IF NOT EXISTS idx_produto_empresa ON produto(empresa_id);
CREATE INDEX IF NOT EXISTS idx_agendamento_empresa_data ON agendamento(empresa_id, data_hora_inicio);
CREATE INDEX IF NOT EXISTS idx_agendamento_profissional ON agendamento(profissional_id);
CREATE INDEX IF NOT EXISTS idx_comanda_empresa ON comanda(empresa_id);
CREATE INDEX IF NOT EXISTS idx_comanda_caixa ON comanda(caixa_id);
CREATE INDEX IF NOT EXISTS idx_mov_fin_empresa_data ON movimentacao_financeira(empresa_id, data_movimentacao);
