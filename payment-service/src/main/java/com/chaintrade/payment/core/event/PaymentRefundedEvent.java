package com.chaintrade.payment.core.event;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PaymentRefundedEvent(@JsonProperty("paymentId") String paymentId,
                                   @JsonProperty("reason") String reason,
                                   @JsonProperty("refundTransactionId") String refundTransactionId) {
}