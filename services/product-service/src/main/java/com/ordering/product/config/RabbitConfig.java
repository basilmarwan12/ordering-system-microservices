package com.ordering.product.config;

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

    // --- Inbound: order.created ---

    @Bean
    public Queue orderCreatedQueue() {
        return new Queue(RabbitTopology.ORDER_CREATED_QUEUE, true);
    }

    @Bean
    public Binding orderCreatedBinding(Queue orderCreatedQueue, TopicExchange ordersExchange) {
        return BindingBuilder.bind(orderCreatedQueue)
                .to(ordersExchange)
                .with(RabbitTopology.ORDER_CREATED_ROUTING_KEY);
    }
}
