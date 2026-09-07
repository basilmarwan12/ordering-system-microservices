package com.ordering.order.service;

import com.ordering.common.events.OrderCreatedEvent;
import com.ordering.order.client.ProductClient;
import com.ordering.order.client.ProductClient.ProductSnapshot;
import com.ordering.order.dto.OrderDtos.CreateItemRequest;
import com.ordering.order.dto.OrderDtos.CreateRequest;
import com.ordering.order.dto.OrderDtos.ItemResponse;
import com.ordering.order.dto.OrderDtos.Response;
import com.ordering.order.event.OrderCreatedPublisher;
import com.ordering.order.exception.OrderNotFoundException;
import com.ordering.order.model.Order;
import com.ordering.order.model.OrderItem;
import com.ordering.order.model.OrderStatus;
import com.ordering.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductClient productClient;
    private final OrderCreatedPublisher publisher;

    /**
     * Creates the order in PENDING status using current prices fetched from
     * product-service (sync REST — we need the price now, this can't wait for
     * an async round trip), then publishes OrderCreatedEvent so product-service
     * can reserve stock asynchronously. The order stays PENDING until the
     * StockReservationResultListener flips it to CONFIRMED or CANCELLED.
     *
     * Note this does NOT decrement stock itself — that happens exactly once,
     * inside product-service's transaction, in response to the event.
     */
    @Transactional
    public Response create(CreateRequest req) {
        Order order = new Order();
        order.setNumber(generateOrderNumber());
        order.setUserId(req.userId());
        order.setPaymentMethod(req.paymentMethod());
        order.setShippingAddress(req.shippingAddress());
        order.setBillingAddress(req.billingAddress());

        BigDecimal subtotal = BigDecimal.ZERO;
        for (CreateItemRequest itemReq : req.items()) {
            ProductSnapshot product = productClient.getProduct(itemReq.productId());
            OrderItem item = new OrderItem(
                    product.id(), product.name(), itemReq.quantity(), product.price());
            order.addItem(item);
            subtotal = subtotal.add(product.price().multiply(BigDecimal.valueOf(itemReq.quantity())));
        }
        order.setSubtotal(subtotal);
        order.setTotalAmount(subtotal.subtract(order.getDiscountAmount()));
        order.setStatus(OrderStatus.PENDING);

        Order saved = orderRepository.save(order);

        publisher.publish(new OrderCreatedEvent(
                saved.getId(),
                saved.getNumber(),
                saved.getItems().stream()
                        .map(i -> new OrderCreatedEvent.Item(i.getProductId(), i.getQuantity()))
                        .toList()
        ));

        log.info("Order {} created as PENDING, awaiting stock reservation", saved.getId());
        return toResponse(saved);
    }

    public Response get(Long id) {
        return toResponse(orderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException(id)));
    }

    public Page<Response> list(Pageable pageable) {
        return orderRepository.findAllByOrderByCreatedAtDesc(pageable).map(this::toResponse);
    }

    private Order findOrThrow(Long id) {
        return orderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));
    }

    private String generateOrderNumber() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private Response toResponse(Order o) {
        List<ItemResponse> items = o.getItems().stream()
                .map(i -> new ItemResponse(i.getProductId(), i.getProductName(), i.getQuantity(), i.getUnitPrice()))
                .toList();
        return new Response(o.getId(), o.getNumber(), o.getUserId(), o.getSubtotal(),
                o.getTotalAmount(), o.getStatus(), items, o.getCreatedAt());
    }
}
