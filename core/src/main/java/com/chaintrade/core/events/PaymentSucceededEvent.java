package com.chaintrade.core.events;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record PaymentSucceededEvent(@JsonProperty("paymentId") String paymentId,
                                    @JsonProperty("orderId") String orderId,
                                    @JsonProperty("customerId") String customerId,
                                    @JsonProperty("cardNumber") String cardNumber,
                                    @JsonProperty("amount") BigDecimal amount,
                                    @JsonProperty("currency") String currency,
                                    @JsonProperty("paymentMethod") String paymentMethod,
                                    @JsonProperty("transactionId") String transactionId) {
}