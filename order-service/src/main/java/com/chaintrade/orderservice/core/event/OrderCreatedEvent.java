package com.chaintrade.orderservice.core.event;

import com.chaintrade.orderservice.core.data.OrderItem;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
public class OrderCreatedEvent {
    @JsonProperty("orderId")
    private String orderId;

    @JsonProperty("customerId")
    private String customerId;

    @JsonProperty("items")
    private List<OrderItem> items;

    @JsonProperty("totalAmount")
    private BigDecimal totalAmount;

    @JsonProperty("shippingAddress")
    private String shippingAddress;

    @JsonCreator
    public OrderCreatedEvent(
            @JsonProperty("orderId") String orderId,
            @JsonProperty("customerId") String customerId,
            @JsonProperty("items") List<OrderItem> items,
            @JsonProperty("totalAmount") BigDecimal totalAmount,
            @JsonProperty("shippingAddress") String shippingAddress) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.items = items;
        this.totalAmount = totalAmount;
        this.shippingAddress = shippingAddress;
    }
} 
