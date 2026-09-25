package com.beautysalon.config.messaging.consumer;

import com.beautysalon.config.messaging.RabbitMQConfig;
import com.beautysalon.config.messaging.event.AgendamentoCriadoEvent;
import com.beautysalon.config.messaging.event.EstoqueBaixoEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.rabbitmq.enabled", havingValue = "true", matchIfMissing = false)
public class NotificationEventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(NotificationEventConsumer.class);

    @RabbitListener(queues = RabbitMQConfig.QUEUE_AGENDAMENTO_NOTIF)
    public void processarNotificacaoAgendamento(AgendamentoCriadoEvent event) {
        logger.info("[RabbitMQ CONSUMER] 📨 Processando notificação assíncrona de agendamento: Cliente '{}' ({}) agendou '{}' para {}",
                event.clienteNome(), event.clienteTelefone(), event.servicoNome(), event.dataHora());

        // Simula processamento em background (ex: disparo assíncrono de e-mail / push notification)
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_ESTOQUE_ALERTA)
    public void processarAlertaEstoqueBaixo(EstoqueBaixoEvent event) {
        logger.warn("[RabbitMQ CONSUMER] ⚠️ Alerta de Estoque Crítico! Produto: '{}' (ID: {}). Saldo atual: {} unidades (Mínimo: {}).",
                event.produtoNome(), event.produtoId(), event.quantidadeAtual(), event.estoqueMinimo());

        // Simula envio de e-mail de alerta para o gerente ou criação de ordem de compra sugerida
    }
}
