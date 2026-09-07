package com.ordering.order.event;

import com.ordering.common.events.OrderCreatedEvent;
import com.ordering.common.events.RabbitTopology;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderCreatedPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publish(OrderCreatedEvent event) {
        rabbitTemplate.convertAndSend(
                RabbitTopology.ORDERS_EXCHANGE,
                RabbitTopology.ORDER_CREATED_ROUTING_KEY,
                event
        );
    }
}
