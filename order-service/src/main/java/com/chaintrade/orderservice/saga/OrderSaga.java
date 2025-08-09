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
            ReserveProductCommand command = new ReserveProductCommand(
                    item.getProductId(),
                    item.getQuantity(),
                    event.getOrderId(),
                    event.getCustomerId()
            );
            log.info("OrderCreatedEvent handled for orderId: {} and productId: {}", command.orderId(), item.getProductId());
            commandGateway.send(command, (commandMessage, commandResultMessage) -> {
                if (commandResultMessage.isExceptional()) {
                    Throwable exception = commandResultMessage.exceptionResult();
                    log.warn("Reservation failed: {}", exception.getMessage());
                    // Start a compensating transaction
                }
            });
        });
    }

    @SagaEventHandler(associationProperty = "orderId")
    public void handle(ProductReservedEvent event) {
        log.info("ProductReservedEvent is called for orderId: {} and productId: {}", event.orderId(), event.productId());
        FetchUserPaymentDetailsQuery query = new FetchUserPaymentDetailsQuery(event.userId());
        queryGateway.query(query, ResponseTypes.instanceOf(UserEntity.class)).thenAccept(user -> {
                    if (user == null) {
                        log.warn("No user found during user payment fetch");
                        // Start compensating transaction
                        return;
                    }
                    log.info("successfully fetched user payment details for user: {}", user.firstName());
                    // Possibly continue the saga here
                })
                .exceptionally(ex -> {
                    log.warn("Query failed: {}", ex.getMessage());
                    // Start compensating transaction
                    return null;
                });
    }
}
