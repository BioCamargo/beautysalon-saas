package com.beautysalon.config.messaging;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "app.rabbitmq.enabled", havingValue = "true", matchIfMissing = false)
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "beautysalon.events.exchange";

    // Filas para eventos de negócio
    public static final String QUEUE_AGENDAMENTO_NOTIF = "beautysalon.agendamento.notificacao.queue";
    public static final String QUEUE_ESTOQUE_ALERTA = "beautysalon.estoque.alerta.queue";

    // Routing Keys
    public static final String ROUTING_KEY_AGENDAMENTO = "beautysalon.agendamento.criado";
    public static final String ROUTING_KEY_ESTOQUE = "beautysalon.estoque.baixo";

    @Bean
    public TopicExchange eventsExchange() {
        return new TopicExchange(EXCHANGE_NAME, true, false);
    }

    @Bean
    public Queue agendamentoNotificacaoQueue() {
        return QueueBuilder.durable(QUEUE_AGENDAMENTO_NOTIF).build();
    }

    @Bean
    public Queue estoqueAlertaQueue() {
        return QueueBuilder.durable(QUEUE_ESTOQUE_ALERTA).build();
    }

    @Bean
    public Binding bindingAgendamento(Queue agendamentoNotificacaoQueue, TopicExchange eventsExchange) {
        return BindingBuilder.bind(agendamentoNotificacaoQueue).to(eventsExchange).with(ROUTING_KEY_AGENDAMENTO);
    }

    @Bean
    public Binding bindingEstoque(Queue estoqueAlertaQueue, TopicExchange eventsExchange) {
        return BindingBuilder.bind(estoqueAlertaQueue).to(eventsExchange).with(ROUTING_KEY_ESTOQUE);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
