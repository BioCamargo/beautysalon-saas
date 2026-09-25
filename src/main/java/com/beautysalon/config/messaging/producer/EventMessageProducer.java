package com.beautysalon.config.messaging.producer;

import com.beautysalon.config.messaging.RabbitMQConfig;
import com.beautysalon.config.messaging.event.AgendamentoCriadoEvent;
import com.beautysalon.config.messaging.event.EstoqueBaixoEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EventMessageProducer {

    private static final Logger logger = LoggerFactory.getLogger(EventMessageProducer.class);

    @Autowired(required = false)
    private RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.enabled:false}")
    private boolean rabbitEnabled;

    public void publicarAgendamentoCriado(AgendamentoCriadoEvent event) {
        if (!rabbitEnabled || rabbitTemplate == null) {
            logger.info("[RabbitMQ OFF] Evento AgendamentoCriado processado em modo síncrono para agendamento #{}", event.agendamentoId());
            return;
        }

        try {
            logger.info("[RabbitMQ PRODUCER] Publicando evento AgendamentoCriado para fila: {}", event);
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_NAME,
                    RabbitMQConfig.ROUTING_KEY_AGENDAMENTO,
                    event
            );
        } catch (Exception e) {
            logger.warn("[RabbitMQ PRODUCER] Falha ao enviar evento para o RabbitMQ: {}", e.getMessage());
        }
    }

    public void publicarAlertaEstoqueBaixo(EstoqueBaixoEvent event) {
        if (!rabbitEnabled || rabbitTemplate == null) {
            logger.info("[RabbitMQ OFF] Alerta de Estoque Baixo para produto '{}' (Qtd: {} / Mín: {})",
                    event.produtoNome(), event.quantidadeAtual(), event.estoqueMinimo());
            return;
        }

        try {
            logger.info("[RabbitMQ PRODUCER] Publicando evento EstoqueBaixo para fila: {}", event);
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_NAME,
                    RabbitMQConfig.ROUTING_KEY_ESTOQUE,
                    event
            );
        } catch (Exception e) {
            logger.warn("[RabbitMQ PRODUCER] Falha ao enviar evento para o RabbitMQ: {}", e.getMessage());
        }
    }
}
