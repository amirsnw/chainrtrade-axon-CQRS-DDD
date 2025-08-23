package com.chaintrade.payment.command;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

public record RefundPaymentCommand(@TargetAggregateIdentifier @JsonProperty("paymentId") String paymentId,
                                   @JsonProperty("reason") String reason) {

}