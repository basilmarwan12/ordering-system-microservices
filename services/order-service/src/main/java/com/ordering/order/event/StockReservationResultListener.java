package com.ordering.order.event;

import com.ordering.common.events.RabbitTopology;
import com.ordering.common.events.StockReservationResultEvent;
import com.ordering.order.model.Order;
import com.ordering.order.model.OrderStatus;
import com.ordering.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockReservationResultListener {

    private final OrderRepository orderRepository;

    @RabbitListener(queues = RabbitTopology.STOCK_RESERVATION_RESULT_QUEUE)
    @Transactional
    public void onStockReservationResult(StockReservationResultEvent event) {
        Order order = orderRepository.findById(event.getOrderId()).orElse(null);
        if (order == null) {
            log.warn("Received stock reservation result for unknown orderId={}", event.getOrderId());
            return;
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            // Already processed (duplicate delivery) — Rabbit gives at-least-once
            // delivery, so this guard makes the handler idempotent.
            log.info("orderId={} already in status={}, ignoring duplicate result", order.getId(), order.getStatus());
            return;
        }

        if (event.isApproved()) {
            order.setStatus(OrderStatus.CONFIRMED);
            log.info("orderId={} confirmed", order.getId());
        } else {
            order.setStatus(OrderStatus.CANCELLED);
            log.warn("orderId={} cancelled: {}", order.getId(), event.getReason());
        }
        orderRepository.save(order);
    }
}
