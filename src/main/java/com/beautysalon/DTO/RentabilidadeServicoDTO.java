package com.beautysalon.DTO;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Rentabilidade e Margem de Lucro por Serviço (Preço - Insumos - Comissão = Lucro Real).
 */
@Data
@Builder
@NoArgsConstructor
public class RentabilidadeServicoDTO {
    private Long servicoId;
    private String nomeServico;
    private long quantidadeExecutada;
    private BigDecimal faturamentoTotal;
    private BigDecimal custoInsumosTotal;
    private BigDecimal comissoesTotal;
    private BigDecimal lucroLiquidoTotal;
    private BigDecimal margemLucroPercentual;

    public RentabilidadeServicoDTO(Long servicoId, String nomeServico, long quantidadeExecutada,
                                   BigDecimal faturamentoTotal, BigDecimal custoInsumosTotal,
                                   BigDecimal comissoesTotal, BigDecimal lucroLiquidoTotal,
                                   BigDecimal margemLucroPercentual) {
        this.servicoId = servicoId;
        this.nomeServico = nomeServico;
        this.quantidadeExecutada = quantidadeExecutada;
        this.faturamentoTotal = faturamentoTotal;
        this.custoInsumosTotal = custoInsumosTotal;
        this.comissoesTotal = comissoesTotal;
        this.lucroLiquidoTotal = lucroLiquidoTotal;
        this.margemLucroPercentual = margemLucroPercentual;
    }
}
