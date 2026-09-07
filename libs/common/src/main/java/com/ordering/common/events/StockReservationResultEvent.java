package com.ordering.common.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Published by product-service in reply to an OrderCreatedEvent.
 * approved = true  -> all lines had sufficient stock, stock has been decremented.
 * approved = false -> at least one line failed; no stock was decremented (all-or-nothing).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StockReservationResultEvent {

    private Long orderId;
    private boolean approved;
    private String reason; // populated when approved = false, e.g. "Insufficient stock for productId=12"
}
