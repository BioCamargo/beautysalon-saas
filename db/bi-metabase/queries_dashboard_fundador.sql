-- ==============================================================================
-- NEXI PRO SaaS — Consultas SQL Oficiais para o Dashboard do Fundador (Metabase)
-- ==============================================================================

-- ------------------------------------------------------------------------------
-- 1. MRR ATUAL & DISTRIBUIÇÃO DE RECEITA RECORRENTE POR PLANO
-- ------------------------------------------------------------------------------
SELECT 
    CASE 
        WHEN plano = 'SOLO' THEN 'Plano Solo (R$ 59,00)'
        WHEN plano = 'STUDIO' THEN 'Plano Studio (R$ 99,00)'
        WHEN plano = 'PREMIUM' OR plano = 'VIP' THEN 'Plano VIP (R$ 179,00)'
        ELSE 'Outros'
    END AS modalidade_plano,
    COUNT(id) AS total_assinantes_ativos,
    SUM(
        CASE 
            WHEN plano = 'SOLO' THEN 59.00
            WHEN plano = 'STUDIO' THEN 99.00
            WHEN plano = 'PREMIUM' OR plano = 'VIP' THEN 179.00
            ELSE 0.00
        END
    ) AS mrr_estimado
FROM empresas
WHERE ativo = true 
  AND status_assinatura = 'ATIVA'
GROUP BY plano
ORDER BY mrr_estimado DESC;


-- ------------------------------------------------------------------------------
-- 2. FUNIL DE ATIVAÇÃO & CONVERSÃO DO TRIAL (ÚLTIMOS 30 DIAS)
-- ------------------------------------------------------------------------------
SELECT 
    status_assinatura AS status_funil,
    COUNT(id) AS total_empresas,
    ROUND(COUNT(id) * 100.0 / SUM(COUNT(id)) OVER(), 1) AS percentual_conversao
FROM empresas
WHERE data_cadastro >= NOW() - INTERVAL '30 days'
GROUP BY status_assinatura
ORDER BY total_empresas DESC;


-- ------------------------------------------------------------------------------
-- 3. GMV (GROSS MERCHANDISE VALUE) & VOLUME POR FORMA DE PAGAMENTO NO SALÃO
-- ------------------------------------------------------------------------------
SELECT 
    COALESCE(forma_pagamento, 'NÃO INFORMADO') AS metodo_pagamento,
    COUNT(id) AS total_transacoes,
    SUM(valor) AS volume_financeiro_total,
    ROUND(AVG(valor), 2) AS ticket_medio_procedimento
FROM pagamentos_comanda
WHERE data_hora >= NOW() - INTERVAL '30 days'
GROUP BY forma_pagamento
ORDER BY volume_financeiro_total DESC;


-- ------------------------------------------------------------------------------
-- 4. RANKING DOS 10 PROCEDIMENTOS / SERVIÇOS MAIS REALIZADOS
-- ------------------------------------------------------------------------------
SELECT 
    ci.descricao_item AS servico,
    COUNT(ci.id) AS vezes_realizado,
    SUM(ci.valor_total) AS faturamento_gerado,
    SUM(ci.valor_comissao) AS comissao_total_paga
FROM comanda_itens ci
JOIN comandas c ON ci.comanda_id = c.id
WHERE c.status = 'PAGA'
  AND ci.tipo = 'SERVICO'
  AND c.data_fechamento >= NOW() - INTERVAL '30 days'
GROUP BY ci.descricao_item
ORDER BY vezes_realizado DESC
LIMIT 10;


-- ------------------------------------------------------------------------------
-- 5. CRESCIMENTO HISTÓRICO DE NOVAS EMPRESAS CADASTRADAS POR MÊS
-- ------------------------------------------------------------------------------
SELECT 
    TO_CHAR(data_cadastro, 'YYYY-MM') AS mes_ano,
    COUNT(id) AS novos_cadastros_mes,
    COUNT(CASE WHEN status_assinatura = 'ATIVA' THEN 1 END) AS convertidos_em_pagos
FROM empresas
GROUP BY TO_CHAR(data_cadastro, 'YYYY-MM')
ORDER BY mes_ano ASC;
