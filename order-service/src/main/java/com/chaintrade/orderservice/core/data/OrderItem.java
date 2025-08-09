package com.chaintrade.orderservice.core.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
public class OrderItem {

    @NotNull
    @JsonProperty("productId")
    private UUID productId;

    @Min(value = 1, message = "Quantity can not be lower than one.")
    @JsonProperty("quantity")
    private int quantity;

    /*@NotNull
    @Min(value = 1, message = "UnitPrice can not be lower than one.")*/
    @JsonProperty("unitPrice")
    private BigDecimal unitPrice;

    @JsonCreator
    public OrderItem(
            @JsonProperty("productId") UUID productId,
            @JsonProperty("quantity") int quantity,
            @JsonProperty("unitPrice") BigDecimal unitPrice) {
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }
}