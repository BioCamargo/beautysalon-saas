package com.beautysalon.DTO;

import lombok.AllArgsConstructor;
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
@AllArgsConstructor
public class RentabilidadeServicoDTO {
    private Long servicoId;
    private String nomeServico;
    private long quantidadeExecutada;
    private BigDecimal faturamentoTotal;
    private BigDecimal custoInsumosTotal;
    private BigDecimal comissoesTotal;
    private BigDecimal lucroLiquidoTotal;
    private BigDecimal margemLucroPercentual;
}
