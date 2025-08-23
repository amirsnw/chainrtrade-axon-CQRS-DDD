package com.chaintrade.core.events;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record ProductReservationFailedEvent(@JsonProperty("productId") UUID productId,
                                            @JsonProperty("quantity") int quantity,
                                            @JsonProperty("orderId") String orderId) {
}
