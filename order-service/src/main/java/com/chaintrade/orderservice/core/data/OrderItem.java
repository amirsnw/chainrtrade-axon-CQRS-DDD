package com.chaintrade.orderservice.core.data;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class OrderItem {
    private String productId;
    private int quantity;
    private BigDecimal unitPrice;
}