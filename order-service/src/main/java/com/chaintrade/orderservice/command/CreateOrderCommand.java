package com.chaintrade.orderservice.command;

import com.chaintrade.orderservice.core.data.OrderItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
public class CreateOrderCommand {
    @TargetAggregateIdentifier
    private String orderId;

    private final String customerId;
    private final List<OrderItem> items;
    private final BigDecimal totalAmount;
    private final String shippingAddress;
} 