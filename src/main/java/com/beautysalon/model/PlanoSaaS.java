package com.beautysalon.model;

import lombok.Getter;

import java.math.BigDecimal;

/**
 * Planos de assinatura comercial do NEXI PRO SaaS.
 */
@Getter
public enum PlanoSaaS {

    SOLO("Plano Solo", new BigDecimal("59.00"), 1, "Para autônomos e pequenos negócios individuais"),
    STUDIO("Plano Studio", new BigDecimal("129.00"), 5, "Para empresas e clínicas com equipe até 5 profissionais"),
    VIP("Plano Premium VIP", new BigDecimal("229.00"), 999, "Equipe ilimitada, DRE contábil e anti-churn"),
    VITALICIO("Contrato Vitalício VIP", BigDecimal.ZERO, 999, "Acesso vitalício integral e ilimitado (Isento de mensalidades)");

    private final String nomeExibicao;
    private final BigDecimal precoMensal;
    private final int limiteProfissionais;
    private final String descricao;

    PlanoSaaS(String nomeExibicao, BigDecimal precoMensal, int limiteProfissionais, String descricao) {
        this.nomeExibicao = nomeExibicao;
        this.precoMensal = precoMensal;
        this.limiteProfissionais = limiteProfissionais;
        this.descricao = descricao;
    }

    public static PlanoSaaS fromString(String value) {
        if (value == null || value.isBlank()) {
            return SOLO;
        }
        String v = value.trim().toUpperCase();
        if ("STARTER".equals(v)) {
            return SOLO;
        }
        try {
            return PlanoSaaS.valueOf(v);
        } catch (IllegalArgumentException e) {
            return SOLO;
        }
    }
}
