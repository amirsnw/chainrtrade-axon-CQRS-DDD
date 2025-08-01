package com.chaintrade.orderservice.command.rest;

import com.chaintrade.orderservice.core.data.OrderItem;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateOrderModel {

    @NotBlank
    private final String customerId;

    @NotEmpty
    @Valid
    private final List<OrderItem> items;

    @Min(value = 1, message = "Price can not be lower than one.")
    private final BigDecimal totalAmount;

    @NotBlank
    private final String shippingAddress;
}
