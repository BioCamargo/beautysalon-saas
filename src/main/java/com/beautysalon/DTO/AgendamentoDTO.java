package com.beautysalon.DTO;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class AgendamentoDTO
{
    private Long id;

    @NotNull(message = "A data e horário são obrigatórios")
    @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime dataHora;

    @NotNull(message = "O cliente é obrigatório")
    private Long clienteId;

    @NotNull(message = "O serviço é obrigatório")
    private Long servicoId;
//-------------------------------------------------
    private String clienteNome;
    private String clienteTelefone;
    private String servicoNome;
    private Long profissionalId;
    private String profissionalNome;
    private String observacoes;
    private String status; // AGENDADO, EM_ATENDIMENTO, CONCLUIDO, CANCELADO
    private Integer duracaoMinutos;

    public Integer getDuracaoMinutos() { return duracaoMinutos; }
    public void setDuracaoMinutos(Integer duracaoMinutos) { this.duracaoMinutos = duracaoMinutos; }

    public Long getProfissionalId() { return profissionalId; }
    public void setProfissionalId(Long profissionalId) { this.profissionalId = profissionalId; }

    public String getProfissionalNome() { return profissionalNome; }
    public void setProfissionalNome(String profissionalNome) { this.profissionalNome = profissionalNome; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    // Getters e Setters
    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}

    public LocalDateTime getDataHora() {return dataHora;}
    public void setDataHora(LocalDateTime dataHora) {this.dataHora = dataHora;}

    public Long getClienteId() {return clienteId;}
    public void setClienteId(Long clienteId) {this.clienteId = clienteId;}

    public Long getServicoId() {return servicoId;}
    public void setServicoId(Long servicoId) {this.servicoId = servicoId;}

    public String getServicoNome() { return servicoNome; }
    public void setServicoNome(String servicoNome) { this.servicoNome = servicoNome; }
    public String getClienteNome() { return clienteNome; }
    public void setClienteNome(String clienteNome) { this.clienteNome = clienteNome; }
    public String getClienteTelefone() { return clienteTelefone; }
    public void setClienteTelefone(String clienteTelefone) { this.clienteTelefone = clienteTelefone; }
}
