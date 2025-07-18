package com.chaintrade.orderservice.core.data;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderEntity implements Serializable {
    @Id
    @Column(unique = true)
    private String orderId;

    private String customerId;
    private String items;
    private BigDecimal totalAmount;
    private String shippingAddress;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;
} 