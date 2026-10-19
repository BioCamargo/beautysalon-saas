package com.beautysalon.model;

import lombok.Getter;

/**
 * Status do ciclo de vida da assinatura do salão no SaaS.
 */
@Getter
public enum StatusAssinatura {

    TRIAL("Período de Testes (7 dias)", "badge-warning", true),
    ATIVA("Assinatura Regular Ativa", "badge-success", true),
    INADIMPLENTE("Fatura em Atraso", "badge-danger", false),
    EXPIRADA("Assinatura Expirada", "badge-danger", false),
    CANCELADA("Assinatura Cancelada", "badge-secondary", false);

    private final String descricao;
    private final String classeBadge;
    private final boolean acessoLiberado;

    StatusAssinatura(String descricao, String classeBadge, boolean acessoLiberado) {
        this.descricao = descricao;
        this.classeBadge = classeBadge;
        this.acessoLiberado = acessoLiberado;
    }
}
