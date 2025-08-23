package com.chaintrade.orderservice.saga;

import com.chaintrade.core.commands.InitiatePaymentCommand;
import com.chaintrade.core.commands.ReleaseProductCommand;
import com.chaintrade.core.commands.ReserveProductCommand;
import com.chaintrade.core.events.PaymentFailedEvent;
import com.chaintrade.core.events.PaymentSucceededEvent;
import com.chaintrade.core.events.ProductReservationFailedEvent;
import com.chaintrade.core.events.ProductReservedEvent;
import com.chaintrade.core.model.UserEntity;
import com.chaintrade.core.query.FetchUserPaymentDetailsQuery;
import com.chaintrade.orderservice.core.event.OrderCreatedEvent;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.deadline.DeadlineManager;
import org.axonframework.deadline.annotation.DeadlineHandler;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.messaging.unitofwork.CurrentUnitOfWork;
import org.axonframework.messaging.unitofwork.UnitOfWork;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.SagaLifecycle;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.spring.stereotype.Saga;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Saga
@Slf4j
@NoArgsConstructor
@Getter
@Setter
@ProcessingGroup("order-saga")
public class OrderSaga {

    @Autowired
    @Getter(AccessLevel.NONE)
    private transient CommandGateway commandGateway;

    @Autowired
    @Getter(AccessLevel.NONE)
    private transient QueryGateway queryGateway;

    @Autowired
    @Getter(AccessLevel.NONE)
    private transient DeadlineManager deadlineManager;

    private String orderId;
    private String userId;
    private int expected;
    private BigDecimal totalAmount;
    private int reserved;
    private int failedCount;
    private Set<UUID> successes = new HashSet<>();
    private String deadlineId;
    private boolean reservationClosed;

    @StartSaga
    @SagaEventHandler(associationProperty = "orderId")
    public void handle(OrderCreatedEvent event) {
        this.orderId = event.getOrderId();
        this.userId = event.getCustomerId();
        this.expected = event.getItems().size();
        this.totalAmount = event.getTotalAmount();
        this.reservationClosed = false;
        this.reserved = 0;
        this.failedCount = 0;
        this.successes.clear();

        log.info("Saga started for orderId={}", event.getOrderId());

        // Associate with all events that will be part of this saga
        SagaLifecycle.associateWith("orderId", event.getOrderId());

        this.deadlineId = deadlineManager.schedule(
                Duration.ofSeconds(20),
                "reservation-timeout",
                event.getOrderId()
        );

        CurrentUnitOfWork.get().afterCommit(t -> {
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
                        // wait for the ProductReservationFailedEvent to be emitted to compensate transaction
                    }
                });
            });
        });
    }

    @SagaEventHandler(associationProperty = "orderId")
    public void handle(ProductReservedEvent event) {
        log.info("ProductReservedEvent is called for orderId: {} and productId: {}", event.orderId(), event.productId());
        if (this.reservationClosed) return; // Ignore late success;
        if (checkSagaState()) return;

        this.reserved++;
        this.successes.add(event.productId());

        log.info("Updated saga state - reserved: {}, successes: {}", this.reserved, this.successes);
        log.info("Current saga state - reserved: {}, failedCount: {}, expected: {}",
                this.reserved, this.failedCount, this.expected);
        checkReservationPhaseComplete(false, event.orderId());
    }

    @SagaEventHandler(associationProperty = "orderId")
    public void handle(ProductReservationFailedEvent event) {
        log.error("Product reservation failed for orderId: {} and productId: {}", event.orderId(), event.productId());
        if (this.reservationClosed) return; // ignore late failures too
        if (checkSagaState()) return;

        this.failedCount++;
        checkReservationPhaseComplete(false, event.orderId());
    }

    @DeadlineHandler(deadlineName = "reservation-timeout")
    public void onTimeout(String orderId) {
        log.error("Timout happened for orderId: {} ", orderId);

        int pending = this.expected - (this.reserved + this.failedCount);

        // treat remaining as failed
        if (pending > 0) this.failedCount += pending;

        this.reservationClosed = true; // Ignore late reservation events
        this.deadlineId = null;

        CurrentUnitOfWork.get().afterCommit(uow -> checkReservationPhaseComplete(true, orderId));
    }

    private void checkReservationPhaseComplete(boolean fromDeadline, String orderId) {
        if (this.failedCount + this.reserved < this.expected) return;

        if (!fromDeadline && this.deadlineId != null) {
            deadlineManager.cancelSchedule("reservation-timeout", this.deadlineId);
        }

        Runnable action = () -> {
            if (this.failedCount == 0) {
                FetchUserPaymentDetailsQuery query = new FetchUserPaymentDetailsQuery(this.userId);
                queryGateway.query(query, ResponseTypes.instanceOf(UserEntity.class)).thenAccept(user -> {
                            if (user == null) {
                                log.warn("No user found during user payment fetch");
                                // Start compensating transaction
                                return;
                            }
                            log.info("successfully fetched user payment details for user: {}", user.firstName());
                            String paymentId = UUID.randomUUID().toString();
                            InitiatePaymentCommand initiatePaymentCommand = new InitiatePaymentCommand(
                                    paymentId,
                                    orderId,
                                    user.userId(),
                                    this.totalAmount,
                                    "Dollar",
                                    "Credit Card"
                            );

                            // Associate payment events with this saga
                            SagaLifecycle.associateWith("paymentId", paymentId);

                            String paymentResult = null;
                            try {
                                paymentResult = commandGateway.sendAndWait(initiatePaymentCommand, 10, TimeUnit.SECONDS);
                            } catch (Exception e) {
                                log.error("Failed to process payment command: {}", e.getMessage());
                                // Start compensating transaction
                            }
                            if (paymentResult == null) {
                                log.error("No payment result found for payment command: {}", initiatePaymentCommand);
                                // Start compensating transaction
                            }
                        })
                        .exceptionally(ex -> {
                            log.warn("Query failed: {}", ex.getMessage());
                            // Start compensating transaction
                            return null;
                        });
            } else {
                log.error("Product Reserve not succeed");
                successes.forEach(uuid -> {
                    ReleaseProductCommand release = new ReleaseProductCommand(uuid, orderId);
                    commandGateway.send(release, (commandMessage, commandResultMessage) -> {
                        if (commandResultMessage.isExceptional()) {
                            Throwable exception = commandResultMessage.exceptionResult();
                            log.warn("Release reservation failed: {}", exception.getMessage());
                        }
                    });
                });
                // Start reject the order
                SagaLifecycle.end();
            }
        };

        // If we’re already inside a UoW, side-effects run after commit
        if (CurrentUnitOfWork.isStarted()
                && CurrentUnitOfWork.get().phase().isBefore(UnitOfWork.Phase.AFTER_COMMIT)) {
            CurrentUnitOfWork.get().afterCommit(uow -> action.run());
        } else {
            action.run();
        }
    }

    @SagaEventHandler(associationProperty = "paymentId")
    public void handle(PaymentSucceededEvent event) {
        log.info("Payment succeeded for orderId: {}, paymentId: {}", this.orderId, event.paymentId());

        // send product confirm reservation for 2-phase reservation and then approve order
        // TODO: Implement product confirmation and order approval logic
        log.info("Payment succeeded - proceeding with order approval");

        // End the saga successfully
        SagaLifecycle.end();
    }

    @SagaEventHandler(associationProperty = "paymentId")
    public void handle(PaymentFailedEvent event) {
        log.error("Payment failed for orderId: {}, reason: {}", this.orderId, event.reason());

        // Start compensating transaction
        compensateFailedPayment(event.paymentId());
    }

    private void compensateFailedPayment(String paymentId) {
        log.info("Starting compensation for failed payment: {}", paymentId);

        // Release all reserved products
        successes.forEach(uuid -> {
            ReleaseProductCommand release = new ReleaseProductCommand(uuid, this.orderId);
            commandGateway.send(release, (commandMessage, commandResultMessage) -> {
                if (commandResultMessage.isExceptional()) {
                    Throwable exception = commandResultMessage.exceptionResult();
                    log.warn("Release reservation failed during compensation: {}", exception.getMessage());
                }
            });
        });

        // End the saga
        SagaLifecycle.end();
    }

    private boolean checkSagaState() {
        // Verify saga state is properly loaded
        if (this.orderId == null) {
            log.error("CRITICAL: Saga orderId is null! Saga state not properly loaded.");
            this.reservationClosed = true;
            return true;
        }
        return false;
    }
}
