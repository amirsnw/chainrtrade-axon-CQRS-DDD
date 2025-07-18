package com.chaintrade.orderservice.query.rest;

import com.chaintrade.orderservice.core.data.OrderItem;
import com.chaintrade.orderservice.core.data.OrderStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderQueryModel {
    private String orderId;
    private String customerId;
    private List<OrderItem> items;
    private BigDecimal totalAmount;
    private String shippingAddress;
    private OrderStatus status;
} 