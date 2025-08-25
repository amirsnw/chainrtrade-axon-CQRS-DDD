package com.chaintrade.core.events;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record ProductReservationReleasedEvent(@JsonProperty("productId") UUID productId,
                                              @JsonProperty("orderId") String orderId,
                                              @JsonProperty("quantity") Integer quantity,
                                              @JsonProperty("reason") String reason) {
}
