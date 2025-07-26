package com.chaintrade.orderservice.query;

import com.chaintrade.orderservice.core.data.OrderEntity;
import com.chaintrade.orderservice.core.data.OrderRepository;
import com.chaintrade.orderservice.core.data.OrderStatus;
import com.chaintrade.orderservice.core.event.OrderCancelledEvent;
import com.chaintrade.orderservice.core.event.OrderCreatedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.messaging.interceptors.ExceptionHandler;
import org.springframework.stereotype.Component;

@Component
@ProcessingGroup("order-group")
public class OrderProjection {
    private final OrderRepository orderRepository;
    private final ObjectMapper objectMapper;

    public OrderProjection(OrderRepository orderRepository, ObjectMapper objectMapper) {
        this.orderRepository = orderRepository;
        this.objectMapper = objectMapper;
    }

    @ExceptionHandler
    public void handle(Exception ex) throws Exception {
        // Log error message
        throw ex;
    }

    @ExceptionHandler(resultType = IllegalArgumentException.class)
    public void handle(IllegalArgumentException ex) {
        // Log error message
        throw ex;
    }

    @EventHandler
    public void on(OrderCreatedEvent event) {
        try {
            OrderEntity order = new OrderEntity(
                    event.getOrderId(),
                    event.getCustomerId(),
                    objectMapper.writeValueAsString(event.getItems()),
                    event.getTotalAmount(),
                    event.getShippingAddress(),
                    OrderStatus.CREATED
            );
            orderRepository.save(order);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error processing order items", e);
        }
    }

    @EventHandler
    public void on(OrderCancelledEvent event) {
        orderRepository.findById(event.getOrderId()).ifPresent(order -> {
            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);
        });
    }
} 