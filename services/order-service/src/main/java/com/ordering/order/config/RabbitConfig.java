package com.ordering.order.config;

import com.ordering.common.events.RabbitTopology;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public TopicExchange ordersExchange() {
        return new TopicExchange(RabbitTopology.ORDERS_EXCHANGE, true, false);
    }

    // --- Inbound: stock.reservation.result ---

    @Bean
    public Queue stockReservationResultQueue() {
        return new Queue(RabbitTopology.STOCK_RESERVATION_RESULT_QUEUE, true);
    }

    @Bean
    public Binding stockReservationResultBinding(Queue stockReservationResultQueue, TopicExchange ordersExchange) {
        return BindingBuilder.bind(stockReservationResultQueue)
                .to(ordersExchange)
                .with(RabbitTopology.STOCK_RESERVATION_RESULT_ROUTING_KEY);
    }
}
