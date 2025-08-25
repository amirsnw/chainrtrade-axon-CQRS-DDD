package com.chaintrade.orderservice.query.rest;

import com.chaintrade.orderservice.query.FindOneOrdersQuery;
import com.chaintrade.orderservice.query.FindOrdersQuery;
import lombok.RequiredArgsConstructor;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
    }

    @GetMapping("/{orderId}")
    public OrderQueryModel getOrder(@PathVariable String orderId) {
        return queryGateway.query(
                new FindOneOrdersQuery(orderId),
                ResponseTypes.instanceOf(OrderQueryModel.class)
        ).join();
    }
} 