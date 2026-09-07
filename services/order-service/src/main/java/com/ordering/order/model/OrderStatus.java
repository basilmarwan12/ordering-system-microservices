package com.ordering.order.model;

/**
 * PENDING     - order row created, OrderCreatedEvent published, awaiting product-service's reply.
 * CONFIRMED   - product-service approved and reserved stock for every line.
 * CANCELLED   - product-service rejected (insufficient stock / unknown product), or a later
 *               step (payment, etc.) failed and triggered a compensating cancellation.
 * SHIPPED / DELIVERED - unchanged from the monolith's downstream fulfillment states.
 */
public enum OrderStatus {
    PENDING,
    CONFIRMED,
    CANCELLED,
    SHIPPED,
    DELIVERED
}
