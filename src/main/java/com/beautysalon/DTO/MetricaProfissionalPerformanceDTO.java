package com.beautysalon.DTO;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
public class MetricaProfissionalPerformanceDTO {
    private Long profissionalId;
    private String nome;
    private String especialidade;
    private BigDecimal percentualComissao;
    private long totalAtendimentos;
    private BigDecimal faturamentoServicos;
    private BigDecimal faturamentoProdutos;
    private BigDecimal faturamentoTotal;
    private BigDecimal totalComissoes;
    private BigDecimal ticketMedio;

    public MetricaProfissionalPerformanceDTO(Long profissionalId, String nome, String especialidade,
                                             BigDecimal percentualComissao, long totalAtendimentos,
                                             BigDecimal faturamentoServicos, BigDecimal faturamentoProdutos,
                                             BigDecimal faturamentoTotal, BigDecimal totalComissoes,
                                             BigDecimal ticketMedio) {
        this.profissionalId = profissionalId;
        this.nome = nome;
        this.especialidade = especialidade;
        this.percentualComissao = percentualComissao;
        this.totalAtendimentos = totalAtendimentos;
        this.faturamentoServicos = faturamentoServicos;
        this.faturamentoProdutos = faturamentoProdutos;
        this.faturamentoTotal = faturamentoTotal;
        this.totalComissoes = totalComissoes;
        this.ticketMedio = ticketMedio;
    }
}
