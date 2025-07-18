package com.chaintrade.orderservice.command.rest;

import com.chaintrade.orderservice.command.CancelOrderCommand;
import com.chaintrade.orderservice.command.CreateOrderCommand;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrdersCommandController {
    private final CommandGateway commandGateway;

    @PostMapping
    public ResponseEntity<CreateOrderCommand> createOrder(@RequestBody @Valid CreateOrderModel dto) {
        CreateOrderCommand command = new CreateOrderCommand(
                UUID.randomUUID().toString(),
                dto.getCustomerId(),
                dto.getItems(),
                dto.getTotalAmount(),
                dto.getShippingAddress()
        );
        String id = commandGateway.sendAndWait(command);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .pathSegment("{id}")
                .buildAndExpand(id)
                .toUri();
        return ResponseEntity.created(location)
                .body(command);
    }

    @PostMapping("/{orderId}/cancel")
    public CompletableFuture<Void> cancelOrder(
            @PathVariable String orderId,
            @RequestParam String reason) {
        return commandGateway.send(new CancelOrderCommand(orderId, reason));
    }
} 