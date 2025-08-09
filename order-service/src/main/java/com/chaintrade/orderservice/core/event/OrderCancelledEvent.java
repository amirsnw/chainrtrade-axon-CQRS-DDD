package com.chaintrade.orderservice.core.event;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OrderCancelledEvent(
        @JsonProperty("orderId") String orderId,
        @JsonProperty("reason") String reason
) {

} 