package com.chaintrade.orderservice.query.rest;

import com.chaintrade.orderservice.query.FindOrdersQuery;
import lombok.RequiredArgsConstructor;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrdersQueryController {
    private final QueryGateway queryGateway;

    @GetMapping
    public List<OrderQueryModel> getAllOrders() {
        return queryGateway.query(
                new FindOrdersQuery(),
                ResponseTypes.multipleInstancesOf(OrderQueryModel.class)
        ).join();
        // return orderRepository.findAll();
    }

    /*@GetMapping("/{orderId}")
    public ResponseEntity<OrderQueryModel> getOrder(@PathVariable String orderId) {
        *//*return orderRepository.findById(orderId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());*//*
    }*/
} 