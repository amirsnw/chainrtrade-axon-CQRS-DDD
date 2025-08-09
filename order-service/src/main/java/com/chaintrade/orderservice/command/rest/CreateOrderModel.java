package com.chaintrade.orderservice.command.rest;

import com.chaintrade.orderservice.core.data.OrderItem;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class CreateOrderModel {

    @NotBlank
    private final String customerId;

    @NotEmpty
    @Valid
    private final List<OrderItem> items;

    @NotBlank
    private final String shippingAddress;
}
