package com.chaintrade.orderservice.core.data;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
public class OrderItem {
    @Null
    private UUID productId;

    @Min(value = 1, message = "Quantity can not be lower than one.")
    private int quantity;

    @NotNull
    @Min(value = 1, message = "UnitPrice can not be lower than one.")
    private BigDecimal unitPrice;
}