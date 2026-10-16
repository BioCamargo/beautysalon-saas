package com.beautysalon.model;

/**
 * Papéis de um usuário dentro de uma empresa (tenant).
 * Substitui o modelo Role anterior por um enum simples e seguro.
 */
public enum TenantRole {
    OWNER("Proprietário"),
    ADMIN("Administrador"),
    RECEPCAO("Recepção / Frente de Caixa"),
    PROFISSIONAL("Profissional / Prestador"),
    FUNCIONARIO("Colaborador Geral");

    private final String descricao;

    TenantRole(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
