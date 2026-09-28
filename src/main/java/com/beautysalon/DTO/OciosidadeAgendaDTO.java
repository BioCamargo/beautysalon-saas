package com.beautysalon.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * Análise de Ociosidade na Grade de Horários da Semana (Terça/Quarta) para Campanhas Relâmpago.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OciosidadeAgendaDTO {
    private LocalDate data;
    private String diaSemana; // Ex: TERÇA-FEIRA
    private int totalHorariosDisponiveis;
    private int totalAgendamentosMarcados;
    private double taxaOcupacaoPercentual;
    private boolean oportunidadePromocao; // true se ocupação < 40%
    private List<String> horariosVagos;
}
