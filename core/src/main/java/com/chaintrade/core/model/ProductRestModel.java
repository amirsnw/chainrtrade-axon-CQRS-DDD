package com.chaintrade.core.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductRestModel(@JsonProperty("productId") UUID productId,
                               @JsonProperty("title") String title,
                               @JsonProperty("price") BigDecimal price,
                               @JsonProperty("quantity") Integer quantity) {
}
