package com.chaintrade.core.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UserEntity(@JsonProperty("firstName") String firstName,
                         @JsonProperty("lastName") String lastName,
                         @JsonProperty("userId") String userId,
                         @JsonProperty("paymentDetails") PaymentDetails paymentDetails) {
}
