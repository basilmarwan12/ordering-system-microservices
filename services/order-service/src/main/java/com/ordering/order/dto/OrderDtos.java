package com.ordering.order.dto;

import com.ordering.order.model.OrderStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class OrderDtos {

    private OrderDtos() {}

    public record CreateItemRequest(
            @NotNull Long productId,
            @NotNull @Min(1) Integer quantity
    ) {}

    public record CreateRequest(
            @NotNull UUID userId,
            String paymentMethod,
            String shippingAddress,
            String billingAddress,
            @NotEmpty List<@Valid CreateItemRequest> items
    ) {}

    public record ItemResponse(
            Long productId,
            String productName,
            Integer quantity,
            BigDecimal unitPrice
    ) {}

    public record Response(
            Long id,
            String number,
            UUID userId,
            BigDecimal subtotal,
            BigDecimal totalAmount,
            OrderStatus status,
            List<ItemResponse> items,
            Instant createdAt
    ) {}
}
