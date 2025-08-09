package com.chaintrade.core.query;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FetchUserPaymentDetailsQuery(@JsonProperty("userId") String userId) {
}
