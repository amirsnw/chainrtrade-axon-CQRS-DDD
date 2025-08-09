package com.chaintrade.core.query;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record FindProductByIdQuery(@JsonProperty("productId") UUID productId) {
}
