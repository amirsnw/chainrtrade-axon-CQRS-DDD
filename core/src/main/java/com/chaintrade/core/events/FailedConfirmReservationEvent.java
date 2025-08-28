package com.chaintrade.core.events;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FailedConfirmReservationEvent(@JsonProperty("orderId") String orderId) {
}
