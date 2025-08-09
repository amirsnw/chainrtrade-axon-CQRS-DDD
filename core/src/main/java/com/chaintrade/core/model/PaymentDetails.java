package com.chaintrade.core.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PaymentDetails(@JsonProperty("name") String name,
                             @JsonProperty("cardNumber") String cardNumber,
                             @JsonProperty("validUntilMonth") Integer validUntilMonth,
                             @JsonProperty("validUntilYear") Integer validUntilYear,
                             @JsonProperty("cvv") String cvv) {

}
