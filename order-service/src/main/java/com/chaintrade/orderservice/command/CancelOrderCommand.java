package com.chaintrade.orderservice.command;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

public record CancelOrderCommand(
        @TargetAggregateIdentifier @JsonProperty("orderId") String orderId,
        @JsonProperty("reason") String reason
) {
} 