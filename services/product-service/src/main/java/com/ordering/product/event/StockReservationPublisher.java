package com.ordering.product.event;

import com.ordering.common.events.RabbitTopology;
import com.ordering.common.events.StockReservationResultEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StockReservationPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publish(StockReservationResultEvent event) {
        rabbitTemplate.convertAndSend(
                RabbitTopology.ORDERS_EXCHANGE,
                RabbitTopology.STOCK_RESERVATION_RESULT_ROUTING_KEY,
                event
        );
    }
}
