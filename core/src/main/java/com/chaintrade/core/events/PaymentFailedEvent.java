package com.chaintrade.core.events;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PaymentFailedEvent(@JsonProperty("paymentId") String paymentId,
                                 @JsonProperty("reason") String reason) {
}