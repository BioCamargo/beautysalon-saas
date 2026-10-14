package com.beautysalon.DTO;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
public class MetricaFormaPagamentoDTO {
    private String formaPagamento;
    private String nomeFormatado;
    private BigDecimal total;
    private long quantidade;
    private BigDecimal percentual;
    private String corBadge;
    private String icone;

    public MetricaFormaPagamentoDTO(String formaPagamento, String nomeFormatado, BigDecimal total,
                                    long quantidade, BigDecimal percentual, String corBadge, String icone) {
        this.formaPagamento = formaPagamento;
        this.nomeFormatado = nomeFormatado;
        this.total = total;
        this.quantidade = quantidade;
        this.percentual = percentual;
        this.corBadge = corBadge;
        this.icone = icone;
    }
}
