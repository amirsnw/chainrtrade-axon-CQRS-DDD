package com.chaintrade.orderservice.command;

import com.chaintrade.orderservice.core.data.OrderLookupEntity;
import com.chaintrade.orderservice.core.data.OrderLookupRepository;
import com.chaintrade.orderservice.core.data.OrderStatus;
import com.chaintrade.orderservice.core.event.OrderCancelledEvent;
import com.chaintrade.orderservice.core.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;

@Component
@ProcessingGroup("order-group")
@RequiredArgsConstructor
public class OrderLookupHandler {

    private final OrderLookupRepository lookUpRepository;

    @EventHandler
    public void on(OrderCreatedEvent event) {
        OrderLookupEntity orderLookupEntity = new OrderLookupEntity(
                event.getOrderId(),
                event.getCustomerId(),
                OrderStatus.CREATED,
                ZonedDateTime.now());
        lookUpRepository.save(orderLookupEntity);
    }

    @EventHandler
    public void on(OrderCancelledEvent event) {
        lookUpRepository.findById(event.orderId()).ifPresent(item -> {
            item.setStatus(OrderStatus.CANCELLED);
            lookUpRepository.save(item);
        });

    }
}
