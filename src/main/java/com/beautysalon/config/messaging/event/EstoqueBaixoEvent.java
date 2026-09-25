package com.beautysalon.config.messaging.event;

import java.io.Serializable;

public record EstoqueBaixoEvent(
        Long produtoId,
        Long empresaId,
        String produtoNome,
        Integer quantidadeAtual,
        Integer estoqueMinimo
) implements Serializable {}
