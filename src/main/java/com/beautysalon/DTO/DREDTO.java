package com.beautysalon.DTO;

import lombok.AllArgsConstructor;
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
@AllArgsConstructor
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
}
