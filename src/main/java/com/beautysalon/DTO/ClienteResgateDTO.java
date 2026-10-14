package com.beautysalon.DTO;

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

    public ClienteResgateDTO(Long clienteId, String nomeCliente, String telefone, String ultimoServicoNome,
                             Long ultimoServicoId, String ultimoProfissionalNome, LocalDate dataUltimoAtendimento,
                             long diasDesdeUltimoAtendimento, int cicloIdealDias, long diasAtraso,
                             String nivelUrgencia, String mensagemSugeridaWhatsapp) {
        this.clienteId = clienteId;
        this.nomeCliente = nomeCliente;
        this.telefone = telefone;
        this.ultimoServicoNome = ultimoServicoNome;
        this.ultimoServicoId = ultimoServicoId;
        this.ultimoProfissionalNome = ultimoProfissionalNome;
        this.dataUltimoAtendimento = dataUltimoAtendimento;
        this.diasDesdeUltimoAtendimento = diasDesdeUltimoAtendimento;
        this.cicloIdealDias = cicloIdealDias;
        this.diasAtraso = diasAtraso;
        this.nivelUrgencia = nivelUrgencia;
        this.mensagemSugeridaWhatsapp = mensagemSugeridaWhatsapp;
    }
}

