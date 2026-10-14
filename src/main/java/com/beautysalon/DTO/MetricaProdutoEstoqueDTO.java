package com.beautysalon.DTO;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
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

    public MetricaProdutoEstoqueDTO(Long produtoId, String nome, String marca, String tipo,
                                   Integer quantidadeEstoque, Integer estoqueMinimo, BigDecimal precoCusto,
                                   BigDecimal precoVenda, BigDecimal valorTotalEstoque, long quantidadeVendidaPeriodo,
                                   BigDecimal faturamentoPeriodo, BigDecimal lucroPeriodo, boolean alertaEstoqueBaixo) {
        this.produtoId = produtoId;
        this.nome = nome;
        this.marca = marca;
        this.tipo = tipo;
        this.quantidadeEstoque = quantidadeEstoque;
        this.estoqueMinimo = estoqueMinimo;
        this.precoCusto = precoCusto;
        this.precoVenda = precoVenda;
        this.valorTotalEstoque = valorTotalEstoque;
        this.quantidadeVendidaPeriodo = quantidadeVendidaPeriodo;
        this.faturamentoPeriodo = faturamentoPeriodo;
        this.lucroPeriodo = lucroPeriodo;
        this.alertaEstoqueBaixo = alertaEstoqueBaixo;
    }
}
