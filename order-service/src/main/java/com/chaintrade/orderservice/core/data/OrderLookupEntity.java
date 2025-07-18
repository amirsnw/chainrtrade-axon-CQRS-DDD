package com.chaintrade.orderservice.core.data;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Calendar;

@Entity
@Table(name = "order_lookup")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderLookupEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private String orderId;

    private String customerId;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private ZonedDateTime dateCreated;
}