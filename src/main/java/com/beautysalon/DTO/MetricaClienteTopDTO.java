package com.beautysalon.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetricaClienteTopDTO {
    private Long clienteId;
    private String nomeCliente;
    private String telefone;
    private long totalVisitas;
    private BigDecimal totalGasto;
    private BigDecimal ticketMedio;
    private LocalDateTime ultimaVisita;
}
