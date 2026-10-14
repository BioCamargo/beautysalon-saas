package com.beautysalon.DTO;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
public class MetricaClienteTopDTO {
    private Long clienteId;
    private String nomeCliente;
    private String telefone;
    private long totalVisitas;
    private BigDecimal totalGasto;
    private BigDecimal ticketMedio;
    private LocalDateTime ultimaVisita;

    public MetricaClienteTopDTO(Long clienteId, String nomeCliente, String telefone,
                                long totalVisitas, BigDecimal totalGasto, BigDecimal ticketMedio,
                                LocalDateTime ultimaVisita) {
        this.clienteId = clienteId;
        this.nomeCliente = nomeCliente;
        this.telefone = telefone;
        this.totalVisitas = totalVisitas;
        this.totalGasto = totalGasto;
        this.ticketMedio = ticketMedio;
        this.ultimaVisita = ultimaVisita;
    }
}
