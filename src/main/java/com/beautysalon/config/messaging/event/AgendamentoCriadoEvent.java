package com.beautysalon.config.messaging.event;

import java.io.Serializable;
import java.time.LocalDateTime;

public record AgendamentoCriadoEvent(
        Long agendamentoId,
        Long empresaId,
        String clienteNome,
        String clienteTelefone,
        String servicoNome,
        String profissionalNome,
        LocalDateTime dataHora
) implements Serializable {}
