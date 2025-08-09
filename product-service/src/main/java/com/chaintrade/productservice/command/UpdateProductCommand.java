package com.chaintrade.productservice.command;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.math.BigDecimal;
import java.util.UUID;

public record UpdateProductCommand(@TargetAggregateIdentifier @JsonProperty("productId") UUID productId,
                                   @JsonProperty("title") String title,
                                   @JsonProperty("price") BigDecimal price,
                                   @JsonProperty("quantity") Integer quantity) {

}
