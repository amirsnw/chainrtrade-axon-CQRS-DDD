package com.chaintrade.core.events;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record ProductReservationConfirmedEvent(@JsonProperty("productId") UUID productId,
                                               @JsonProperty("orderId") String orderId,
                                               @JsonProperty("quantity") Integer quantity) {
}
