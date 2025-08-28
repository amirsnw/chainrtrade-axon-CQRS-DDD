package com.chaintrade.orderservice;

import com.chaintrade.core.events.OrderApprovedEvent;
import com.chaintrade.orderservice.command.ApproveOrderCommand;
import com.chaintrade.orderservice.command.CancelOrderCommand;
import com.chaintrade.orderservice.command.CreateOrderCommand;
import com.chaintrade.orderservice.core.data.OrderItem;
import com.chaintrade.orderservice.core.data.OrderStatus;
import com.chaintrade.orderservice.core.event.OrderCancelledEvent;
import com.chaintrade.orderservice.core.event.OrderCreatedEvent;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;
import org.springframework.beans.BeanUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;

@Aggregate(type = "order")
@NoArgsConstructor
@Slf4j
public class OrderAggregate {

    @AggregateIdentifier
    private String orderId;

    private String customerId;
    private List<OrderItem> items;
    private BigDecimal totalAmount;
    private String shippingAddress;
    private OrderStatus status;

    @CommandHandler
    public OrderAggregate(CreateOrderCommand command) {
        OrderCreatedEvent event = new OrderCreatedEvent();
        BeanUtils.copyProperties(command, event);
        AggregateLifecycle.apply(event);
        if (command.items().stream().anyMatch(item -> !StringUtils.hasLength(String.valueOf(item.getProductId())))) {
            throw new IllegalArgumentException("Product id is required");
        }
    }

    @CommandHandler
    public void handle(CancelOrderCommand command) {
        if (status == OrderStatus.CANCELLED) {
            log.warn("Order is already cancelled");
            return;
        }
        AggregateLifecycle.apply(new OrderCancelledEvent(
                command.orderId(),
                command.reason()
        ));
    }

    @CommandHandler
    public void handle(ApproveOrderCommand command) {
        if (status == OrderStatus.APPROVED) {
            log.warn("Order is already cancelled");
            return;
        }
        AggregateLifecycle.apply(new OrderApprovedEvent(
                command.orderId()
        ));
    }

    @EventSourcingHandler
    public void on(OrderCreatedEvent event) {
        this.orderId = event.getOrderId();
        this.customerId = event.getCustomerId();
        this.items = event.getItems();
        this.totalAmount = event.getTotalAmount();
        this.shippingAddress = event.getShippingAddress();
        this.status = OrderStatus.CREATED;
    }

    @EventSourcingHandler
    public void on(OrderCancelledEvent event) {
        this.status = OrderStatus.CANCELLED;
    }

    @EventSourcingHandler
    public void on(OrderApprovedEvent event) {
        this.status = OrderStatus.APPROVED;
    }
} 