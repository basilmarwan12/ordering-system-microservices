package com.ordering.common.events;

/**
 * Single source of truth for exchange/queue/routing-key names.
 * Both order-service and product-service declare their own beans using these
 * constants so a typo can't silently split the topology in two.
 */
public final class RabbitTopology {

    private RabbitTopology() {}

    public static final String ORDERS_EXCHANGE = "orders.exchange";

    public static final String ORDER_CREATED_ROUTING_KEY = "order.created";
    public static final String STOCK_RESERVATION_RESULT_ROUTING_KEY = "stock.reservation.result";

    // product-service listens here
    public static final String ORDER_CREATED_QUEUE = "product-service.order-created";
    // order-service listens here
    public static final String STOCK_RESERVATION_RESULT_QUEUE = "order-service.stock-reservation-result";
}
