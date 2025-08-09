package com.chaintrade.orderservice.command;

import com.chaintrade.orderservice.core.data.OrderItem;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.math.BigDecimal;
import java.util.List;

public record CreateOrderCommand(@TargetAggregateIdentifier @JsonProperty("orderId") String orderId,
                                 @JsonProperty("customerId") String customerId,
                                 @JsonProperty("items") List<OrderItem> items,
                                 @JsonProperty("totalAmount") BigDecimal totalAmount,
                                 @JsonProperty("shippingAddress") String shippingAddress) {
    @JsonCreator
    public CreateOrderCommand {
    }
} 