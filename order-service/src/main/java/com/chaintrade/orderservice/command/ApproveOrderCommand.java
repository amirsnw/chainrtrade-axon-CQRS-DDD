package com.chaintrade.orderservice.command;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

public record ApproveOrderCommand(
        @TargetAggregateIdentifier @JsonProperty("orderId") String orderId
) {
} 