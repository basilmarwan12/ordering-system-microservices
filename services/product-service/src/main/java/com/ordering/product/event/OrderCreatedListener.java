package com.ordering.product.event;

import com.ordering.common.events.OrderCreatedEvent;
import com.ordering.common.events.RabbitTopology;
import com.ordering.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCreatedListener {

    private final ProductService productService;

    @RabbitListener(queues = RabbitTopology.ORDER_CREATED_QUEUE)
    public void onOrderCreated(OrderCreatedEvent event) {
        log.info("Received OrderCreatedEvent for orderId={}", event.getOrderId());
        productService.reserveStockForOrder(event);
        // Any uncaught exception here goes back onto the queue via Spring AMQP's
        // default retry/DLQ behavior (configurable in application.yml) rather than
        // silently dropping the message.
    }
}
