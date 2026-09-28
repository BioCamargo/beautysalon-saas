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
public class MetricaAgendamentoDTO {
    private long totalAgendados;
    private long concluidos;
    private long cancelados;
    private long pendentes;
    private BigDecimal taxaConversao;
    private BigDecimal taxaCancelamento;
    private BigDecimal perdaFinanceiraEstimada;
}
