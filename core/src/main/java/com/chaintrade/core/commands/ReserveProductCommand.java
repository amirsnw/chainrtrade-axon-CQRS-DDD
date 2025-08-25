package com.chaintrade.core.commands;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.time.Instant;
import java.util.UUID;

public record ReserveProductCommand(@TargetAggregateIdentifier @JsonProperty("productId") UUID productId,
                                    @JsonProperty("quantity") int quantity,
                                    @JsonProperty("orderId") String orderId,
                                    @JsonProperty("expiresAt") Instant expiresAt,
                                    @JsonProperty("reservationWindow") UUID reservationWindow) {
}
