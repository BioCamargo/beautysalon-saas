package com.beautysalon.DTO.rest;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class AgendamentoRestDTO {

    public record Request(
            @NotNull(message = "A data e hora do agendamento são obrigatórias")
            LocalDateTime dataHora,

            @NotNull(message = "O ID do cliente é obrigatório")
            Long clienteId,

            @NotNull(message = "O ID do serviço é obrigatório")
            Long servicoId,

            Long profissionalId,

            String observacoes
    ) {}

    public record Response(
            Long id,
            LocalDateTime dataHora,
            String status,
            String observacoes,
            Long clienteId,
            String clienteNome,
            Long servicoId,
            String servicoNome,
            Long profissionalId,
            String profissionalNome
    ) {}

    public record StatusUpdateRequest(
            @NotNull(message = "O novo status é obrigatório")
            String status
    ) {}
}
