package com.chaintrade.orderservice.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Data
@AllArgsConstructor
public class CancelOrderCommand {
    @TargetAggregateIdentifier
    private String orderId;

    private String reason;
} 