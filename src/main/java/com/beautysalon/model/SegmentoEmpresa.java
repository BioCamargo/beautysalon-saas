package com.beautysalon.model;

/**
 * Segmento de atuação do estabelecimento no sistema SaaS.
 * Define a personalização de termos, rótulos e fichas de anamnese.
 */
public enum SegmentoEmpresa {
    SALAO_BELEZA("Salão de Beleza / Cabelo", "bi-scissors", "Ficha Química & Anamnese Capilar",
            "Fórmula & Proporções Utilizadas", "Ex: 50g Pó Descolorante + 100ml OX 30vol (1:2)"),
    BARBEARIA("Barbearia", "bi-brush", "Ficha Técnica & Visagismo", "Produtos & Lâminas / Pigmentos",
            "Ex: Pigmento castanho escuro barba, navalhete descartável"),
    ESTETICA_SPA("Clínica de Estética & Spa", "bi-flower1", "Ficha de Avaliação & Protocolo Estético",
            "Protocolo & Ativos Utilizados", "Ex: Ácido glicólico 10%, Laser 15J/cm², Sérum Hialurônico"),
    LASH_BROW("Lash & Brow / Micropigmentação", "bi-eye", "Ficha de Mapeamento & Anamnese",
            "Mapeamento, Curvatura & Pigmentos", "Ex: Pigmento Marrom Café 4 gotas, Curvatura D 0.07 (8-12mm)"),
    ESMALTERIA_PODOLOGIA("Esmalteria & Podologia", "bi-hand-index-thumb", "Ficha de Cuidados & Podologia",
            "Produtos & Procedimento Aplicado", "Ex: Desbaste plantar com bisturi 22, óleo de melaleuca"),
    GERAL("Estúdio Multidisciplinar / Outros", "bi-stars", "Ficha de Anamnese & Prontuário Técnico",
            "Produtos, Protocolos & Materiais", "Ex: Materiais, tonalidades, marcas e parâmetros aplicados");

    private final String descricao;
    private final String icone;
    private final String tituloAnamnese;
    private final String labelFormulaProtocolo;
    private final String placeholderFormula;

    SegmentoEmpresa(String descricao, String icone, String tituloAnamnese, String labelFormulaProtocolo,
            String placeholderFormula) {
        this.descricao = descricao;
        this.icone = icone;
        this.tituloAnamnese = tituloAnamnese;
        this.labelFormulaProtocolo = labelFormulaProtocolo;
        this.placeholderFormula = placeholderFormula;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getIcone() {
        return icone;
    }

    public String getTituloAnamnese() {
        return tituloAnamnese;
    }

    public String getLabelFormulaProtocolo() {
        return labelFormulaProtocolo;
    }

    public String getPlaceholderFormula() {
        return placeholderFormula;
    }
}
