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
public class MetricaProdutoEstoqueDTO {
    private Long produtoId;
    private String nome;
    private String marca;
    private String tipo;
    private Integer quantidadeEstoque;
    private Integer estoqueMinimo;
    private BigDecimal precoCusto;
    private BigDecimal precoVenda;
    private BigDecimal valorTotalEstoque;
    private long quantidadeVendidaPeriodo;
    private BigDecimal faturamentoPeriodo;
    private BigDecimal lucroPeriodo;
    private boolean alertaEstoqueBaixo;
}
