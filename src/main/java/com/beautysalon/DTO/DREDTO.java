package com.beautysalon.DTO;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para o DRE Simplificado e Análise de Rentabilidade de Serviços.
 */
@Data
@Builder
@NoArgsConstructor
public class DREDTO {
    private BigDecimal receitaBrutaServicos;
    private BigDecimal receitaBrutaProdutos;
    private BigDecimal receitaBrutaTotal;
    private BigDecimal deducoesDescontos;
    private BigDecimal receitaLiquida;

    // Custos Variáveis
    private BigDecimal totalComissoesProfissionais;
    private BigDecimal custoInsumosServicos;
    private BigDecimal custoProdutosVendidos;
    private BigDecimal totalCustosVariaveis;

    // Margem de Contribuição / Lucro Bruto
    private BigDecimal margemContribuicao;
    private BigDecimal margemContribuicaoPercentual;

    // Despesas Fixas / Operacionais do Caixa
    private BigDecimal despesasOperacionais;
    private BigDecimal resultadoLiquido;
    private BigDecimal lucratividadePercentual;

    public DREDTO(BigDecimal receitaBrutaServicos, BigDecimal receitaBrutaProdutos, BigDecimal receitaBrutaTotal,
                  BigDecimal deducoesDescontos, BigDecimal receitaLiquida, BigDecimal totalComissoesProfissionais,
                  BigDecimal custoInsumosServicos, BigDecimal custoProdutosVendidos, BigDecimal totalCustosVariaveis,
                  BigDecimal margemContribuicao, BigDecimal margemContribuicaoPercentual,
                  BigDecimal despesasOperacionais, BigDecimal resultadoLiquido, BigDecimal lucratividadePercentual) {
        this.receitaBrutaServicos = receitaBrutaServicos;
        this.receitaBrutaProdutos = receitaBrutaProdutos;
        this.receitaBrutaTotal = receitaBrutaTotal;
        this.deducoesDescontos = deducoesDescontos;
        this.receitaLiquida = receitaLiquida;
        this.totalComissoesProfissionais = totalComissoesProfissionais;
        this.custoInsumosServicos = custoInsumosServicos;
        this.custoProdutosVendidos = custoProdutosVendidos;
        this.totalCustosVariaveis = totalCustosVariaveis;
        this.margemContribuicao = margemContribuicao;
        this.margemContribuicaoPercentual = margemContribuicaoPercentual;
        this.despesasOperacionais = despesasOperacionais;
        this.resultadoLiquido = resultadoLiquido;
        this.lucratividadePercentual = lucratividadePercentual;
    }
}
