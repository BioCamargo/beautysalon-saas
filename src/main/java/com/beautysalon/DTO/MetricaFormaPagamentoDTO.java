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
public class MetricaFormaPagamentoDTO {
    private String formaPagamento;
    private String nomeFormatado;
    private BigDecimal total;
    private long quantidade;
    private BigDecimal percentual;
    private String corBadge;
    private String icone;
}
