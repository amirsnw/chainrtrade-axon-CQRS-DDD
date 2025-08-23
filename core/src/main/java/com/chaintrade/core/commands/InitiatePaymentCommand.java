package com.chaintrade.core.commands;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.math.BigDecimal;

public record InitiatePaymentCommand(@TargetAggregateIdentifier @JsonProperty("paymentId") String paymentId,
                                     @JsonProperty("orderId") String orderId,
                                     @JsonProperty("customerId") String customerId,
                                     @JsonProperty("amount") BigDecimal amount,
                                     @JsonProperty("currency") String currency,
                                     @JsonProperty("paymentMethod") String paymentMethod) {

}