package com.chaintrade.core.events;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record ProductReleaseReservedEvent(@JsonProperty("productId") UUID productId,
                                          @JsonProperty("orderId") String orderId) {
}
