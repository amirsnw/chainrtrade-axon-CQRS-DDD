package com.chaintrade.core.commands;

import lombok.Builder;
import lombok.Data;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.util.UUID;

@Data
@Builder
public class ReserveProductCommand {

    @TargetAggregateIdentifier
    private final UUID productId;
    private final int quantity;
    private final String orderId;
    private final String userId;

}
