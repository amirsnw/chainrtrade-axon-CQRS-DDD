package com.chaintrade.core.commands;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.util.UUID;

public record ReleaseProductCommand(@TargetAggregateIdentifier @JsonProperty("productId") UUID productId,
                                    @JsonProperty("orderId") String orderId,
                                    @JsonProperty("reason") String reason) {
}
