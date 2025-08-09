package com.chaintrade.orderservice.command.rest;

import com.chaintrade.orderservice.command.CancelOrderCommand;
import com.chaintrade.orderservice.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrdersCommandController {
    private final CommandGateway commandGateway;
    private final OrderService orderService;

    @PostMapping
    public CompletableFuture<ResponseEntity<Void>> createOrder(@RequestBody @Valid CreateOrderModel dto) {
        return orderService.placeOrder(dto).thenApply(uuid -> {
            URI location = ServletUriComponentsBuilder
                    .fromCurrentRequest()
                    .pathSegment("{id}")
                    .buildAndExpand(uuid)
                    .toUri();
            return ResponseEntity.created(location).build();
        });
    }

    @PostMapping("/{orderId}/cancel")
    public CompletableFuture<Void> cancelOrder(
            @PathVariable String orderId,
            @RequestParam String reason) {
        return commandGateway.send(new CancelOrderCommand(orderId, reason));
    }
} 