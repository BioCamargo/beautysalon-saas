package com.beautysalon.model;

/**
 * Tipos de entrada suportados pelo Form Builder de Anamnese.
 */
public enum TipoCampoCustomizado {
    TEXTO("Texto Curto", "bi-fonts"),
    TEXTO_LONGO("Texto Longo / Observação", "bi-card-text"),
    SIM_NAO("Sim / Não (Booleano)", "bi-toggle-on"),
    SELECAO("Lista de Seleção (Dropdown)", "bi-menu-button-wide"),
    NUMERO("Número / Quantidade", "bi-123");

    private final String descricao;
    private final String icone;

    TipoCampoCustomizado(String descricao, String icone) {
        this.descricao = descricao;
        this.icone = icone;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getIcone() {
        return icone;
    }
}
