package com.beautysalon.DTO;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
public class MetricaAgendamentoDTO {
    private long totalAgendados;
    private long concluidos;
    private long cancelados;
    private long pendentes;
    private BigDecimal taxaConversao;
    private BigDecimal taxaCancelamento;
    private BigDecimal perdaFinanceiraEstimada;

    public MetricaAgendamentoDTO(long totalAgendados, long concluidos, long cancelados, long pendentes,
                                 BigDecimal taxaConversao, BigDecimal taxaCancelamento, BigDecimal perdaFinanceiraEstimada) {
        this.totalAgendados = totalAgendados;
        this.concluidos = concluidos;
        this.cancelados = cancelados;
        this.pendentes = pendentes;
        this.taxaConversao = taxaConversao;
        this.taxaCancelamento = taxaCancelamento;
        this.perdaFinanceiraEstimada = perdaFinanceiraEstimada;
    }
}
