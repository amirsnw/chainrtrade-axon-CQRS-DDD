package com.chaintrade.core.events;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

public record ProductReservedEvent(@JsonProperty("productId") UUID productId,
                                   @JsonProperty("quantity") int quantity,
                                   @JsonProperty("orderId") String orderId,
                                   @JsonProperty("expiresAt") Instant expiresAt,
                                   @JsonProperty("reservationWindow") UUID reservationWindow) {
}
