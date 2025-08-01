package com.chaintrade.orderservice.saga;

import com.chaintrade.core.commands.ReserveProductCommand;
import com.chaintrade.core.events.ProductReservedEvent;
import com.chaintrade.core.model.UserEntity;
import com.chaintrade.core.query.FetchUserPaymentDetailsQuery;
import com.chaintrade.orderservice.core.event.OrderCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.spring.stereotype.Saga;
import org.springframework.beans.factory.annotation.Autowired;

@Saga
@Slf4j
public class OrderSaga {

    @Autowired
    private transient CommandGateway commandGateway;

    @Autowired
    private transient QueryGateway queryGateway;

    @StartSaga
    @SagaEventHandler(associationProperty = "orderId")
    public void handle(OrderCreatedEvent event) {
        event.getItems().forEach(item -> {
            ReserveProductCommand command = ReserveProductCommand.builder()
                    .orderId(event.getOrderId())
                    .productId(item.getProductId())
                    .quantity(item.getQuantity())
                    .userId(event.getCustomerId())
                    .build();
            log.info("OrderCreatedEvent handled for orderId: {} and productId: {}", command.getOrderId(), item.getProductId());
            commandGateway.send(command, (commandMessage, commandResultMessage) -> {
                if (commandResultMessage.isExceptional()) {
                    // Start a compensating transaction
                }
            });
        });
    }

    @SagaEventHandler(associationProperty = "orderId")
    public void handle(ProductReservedEvent event) {
        log.info("ProductReservedEvent is called for orderId: {} and productId: {}", event.getOrderId(), event.getProductId());
        FetchUserPaymentDetailsQuery query = new FetchUserPaymentDetailsQuery(event.getOrderId());
        UserEntity user;
        try {
            user = queryGateway.query(query, ResponseTypes.instanceOf(UserEntity.class)).join();
        } catch (Exception e) {
            log.info("message: {}", e.getMessage());
            // Start compensating transaction
            return;
        }
        if (user == null) {
            // Start compensating transaction
            return;
        }
        log.info("successfully fetched user payment details for user: {}", user.getFirstName());
    }
}
