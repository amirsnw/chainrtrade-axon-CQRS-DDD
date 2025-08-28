package com.chaintrade.core.events;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OrderApprovedEvent(
        @JsonProperty("orderId") String orderId
) {

} 