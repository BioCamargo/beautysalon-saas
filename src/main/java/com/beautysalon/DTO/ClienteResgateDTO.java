package com.beautysalon.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Representa um cliente elegível para resgate automático com base no ciclo de retorno do serviço.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClienteResgateDTO {
    private Long clienteId;
    private String nomeCliente;
    private String telefone;
    private String ultimoServicoNome;
    private Long ultimoServicoId;
    private String ultimoProfissionalNome;
    private LocalDate dataUltimoAtendimento;
    private long diasDesdeUltimoAtendimento;
    private int cicloIdealDias;
    private long diasAtraso; // Quantos dias passou da data estimada de retorno
    private String nivelUrgencia; // MODERADO, ALTO, CRITICO
    private String mensagemSugeridaWhatsapp;
}
