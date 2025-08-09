package com.chaintrade.productservice.core.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
public class ProductCreatedEvent {
    @JsonProperty("productId")
    private UUID productId;

    @JsonProperty("title")
    private String title;

    @JsonProperty("price")
    private BigDecimal price;

    @JsonProperty("quantity")
    private Integer quantity;

    @JsonCreator
    public ProductCreatedEvent(
            @JsonProperty("productId") UUID productId,
            @JsonProperty("title") String title,
            @JsonProperty("price") BigDecimal price,
            @JsonProperty("quantity") Integer quantity) {
        this.productId = productId;
        this.title = title;
        this.price = price;
        this.quantity = quantity;
    }
}
