package com.beautysalon.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
}
