package com.chaintrade.orderservice.saga;

import com.chaintrade.core.commands.ConfirmProductReservationCommand;
import com.chaintrade.core.commands.InitiatePaymentCommand;
import com.chaintrade.core.commands.ReleaseProductCommand;
import com.chaintrade.core.commands.ReserveProductCommand;
import com.chaintrade.core.events.*;
import com.chaintrade.core.model.UserEntity;
import com.chaintrade.core.query.FetchUserPaymentDetailsQuery;
import com.chaintrade.orderservice.command.ApproveOrderCommand;
import com.chaintrade.orderservice.command.CancelOrderCommand;
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
import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.GenericEventMessage;
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
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

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
    private transient EventBus eventBus;

    @Autowired
    @Getter(AccessLevel.NONE)
    private transient DeadlineManager deadlineManager;

    private String orderId;
    private String paymentId;
    private String userId;
    private int expected;
    private BigDecimal totalAmount;
    private int reserved;
    private int failedCount;
    private Set<Hold> successes = new HashSet<>();
    private Map<UUID, UUID> reservationWindows = new HashMap<>();
    private String deadlineId;
    private boolean reservationClosed;
    private boolean paymentProcessed;

    @StartSaga
    @SagaEventHandler(associationProperty = "orderId")
    public void handle(OrderCreatedEvent event) {
        this.orderId = event.getOrderId();
        this.userId = event.getCustomerId();
        this.expected = event.getItems().size();
        this.totalAmount = event.getTotalAmount();
        this.reservationClosed = false;
        this.paymentProcessed = false;
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
                UUID window = ensureReservationWindowFor(item.getProductId());
                ReserveProductCommand command = new ReserveProductCommand(
                        item.getProductId(),
                        item.getQuantity(),
                        event.getOrderId(),
                        ZonedDateTime.now().plusSeconds(20).toInstant(),
                        window
                );
                log.info("OrderCreatedEvent handled for orderId: {} and productId: {}", command.orderId(), item.getProductId());
                commandGateway.send(command, (commandMessage, commandResultMessage) -> {
                    if (commandResultMessage.isExceptional()) {
                        Throwable exception = commandResultMessage.exceptionResult();
                        log.warn("Reservation failed: {}", exception.getMessage());
                        // wait for the ProductReservationFailedEvent to be emitted after timeout
                        // to compensate transaction
                    }
                });
            });
        });
    }

    @SagaEventHandler(associationProperty = "orderId")
    public void handle(ProductReservedEvent event) {
        log.info("ProductReservedEvent is called for orderId: {} and productId: {}", event.orderId(), event.productId());
        if (hasReservationShortCircuited()) return;

        this.reserved++;
        this.successes.add(new Hold(event.productId(), event.quantity()));

        log.info("Updated saga state - reserved: {}, successes: {}", this.reserved, this.successes);
        log.info("Current saga state - reserved: {}, failedCount: {}, expected: {}",
                this.reserved, this.failedCount, this.expected);
        checkReservationPhaseComplete(event.orderId());
    }

    @SagaEventHandler(associationProperty = "orderId")
    public void handle(ProductReservationReleasedEvent event) {
        log.error("Product reservation failed for orderId: {} and productId: {}", event.orderId(), event.productId());
        if (hasReservationShortCircuited()) return;

        this.failedCount++;
        checkReservationPhaseComplete(event.orderId());
    }

    @DeadlineHandler(deadlineName = "reservation-timeout")
    public void onTimeout(String orderId) {
        log.error("Timout happened for orderId: {} ", orderId);

        int pending = this.expected - (this.reserved + this.failedCount);
        if (pending > 0) this.failedCount += pending; // treat remaining as failed
        this.reservationClosed = true; // Ignore late reservation events

        CurrentUnitOfWork.get().afterCommit(uow -> compensateReservation("saga-timeout"));
    }

    private void checkReservationPhaseComplete(String orderId) {
        if (this.failedCount + this.reserved < this.expected) return;

        safeCancelDeadlineIfAny();

        if (this.failedCount == 0) {
            FetchUserPaymentDetailsQuery query = new FetchUserPaymentDetailsQuery(this.userId);
            UserEntity user = queryGateway.query(query, ResponseTypes.instanceOf(UserEntity.class)).join();
            if (user == null) {
                log.warn("No user found during user payment fetch");
                // Start compensating transaction
                return;
            }
            log.info("successfully fetched user payment details for user: {}", user.firstName());
            this.paymentId = UUID.randomUUID().toString();

            // Associate payment events with this saga
            SagaLifecycle.associateWith("paymentId", paymentId);

            CurrentUnitOfWork.get().afterCommit(uow -> {
                InitiatePaymentCommand initiatePaymentCommand = new InitiatePaymentCommand(
                        paymentId,
                        orderId,
                        user.userId(),
                        user.paymentDetails().cardNumber(),
                        this.totalAmount,
                        "Dollar",
                        "Credit Card"
                );
                commandGateway.send(initiatePaymentCommand, (commandMessage, commandResultMessage) -> {
                    if (commandResultMessage.isExceptional()) {
                        Throwable exception = commandResultMessage.exceptionResult();
                        log.error("Failed to process payment command: {}", exception.getMessage());
                        compensateReservation("payment-failure");
                    }
                });
            });
        } else {
            compensateReservation("reservation-failure");
        }
    }

    @SagaEventHandler(associationProperty = "paymentId")
    public void handle(PaymentSucceededEvent event) {
        if (this.paymentProcessed) {
            log.info("PaymentSucceededEvent already processed for orderId={}", orderId);
            return;
        }
        this.paymentProcessed = true;
        this.paymentId = event.paymentId();
        log.info("Payment succeeded for orderId: {}, paymentId: {}", this.orderId, event.paymentId());

        // send product confirm reservation for 2-phase reservation
        CurrentUnitOfWork.get().afterCommit(uow -> {
            var confirmFutures = successes.stream()
                    .map(hold -> commandGateway.<String>send(
                            new ConfirmProductReservationCommand(hold.productId(), orderId, hold.quantity())
                    )).toList();
            CompletableFuture.allOf(confirmFutures.toArray(CompletableFuture[]::new))
                    .thenApply(v -> confirmFutures.stream().map(cf -> {
                                try {
                                    return cf.join();
                                } catch (CompletionException ex) {
                                    return ex;
                                }
                            }).toList()
                    ).thenAccept(results -> {
                        boolean anyFailed = results.stream().anyMatch(r -> r instanceof Throwable);
                        if (anyFailed) {
                            log.warn("One or more confirmations failed; compensating.");
                            eventBus.publish(
                                    GenericEventMessage.asEventMessage(new FailedConfirmReservationEvent(orderId))
                            );
                        } else {
                            commandGateway.send(new ApproveOrderCommand(orderId));
                            log.info("All confirmations succeeded. ApproveOrder sent for {}", orderId);
                        }
                    });
        });
    }

    @SagaEventHandler(associationProperty = "orderId")
    public void handle(OrderApprovedEvent event) {
        log.info("Order successfully complete for orderId: {}", this.orderId);
        SagaLifecycle.end();
    }

    @SagaEventHandler(associationProperty = "paymentId")
    public void handle(PaymentFailedEvent event) {
        if (this.paymentProcessed) {
            log.info("PaymentFailedEvent already processed for orderId={}", orderId);
            return;
        }
        this.paymentProcessed = true;
        log.error("Payment failed for orderId: {}, reason: {}", this.orderId, event.reason());

        CurrentUnitOfWork.get().afterCommit(uow -> compensateFailedPayment(event.paymentId()));
    }

    private boolean hasReservationShortCircuited() {
        // Verify saga state is properly loaded
        if (this.orderId == null) {
            log.error("CRITICAL: Saga orderId is null! Saga state not properly loaded.");
            return true;
        }
        if (this.reservationClosed) {
            return true; // Ignore late success;
        }
        if (this.paymentProcessed) {
            log.info("Payment already processed for orderId={}", orderId);
            return true;
        }
        return false;
    }

    private UUID ensureReservationWindowFor(UUID productId) {
        return reservationWindows.computeIfAbsent(productId, pid -> UUID.randomUUID());
    }

    private void safeCancelDeadlineIfAny() {
        if (deadlineId == null) return;
        if (CurrentUnitOfWork.isStarted()
                && CurrentUnitOfWork.get().phase().isBefore(UnitOfWork.Phase.PREPARE_COMMIT)) {
            deadlineManager.cancelSchedule("reservation-timeout", deadlineId);
        }
        deadlineId = null;
    }

    // Compensation helper methods
    private void compensateReservation(String reason) {
        Runnable action = () -> {
            successes.forEach(hold -> {
                ReleaseProductCommand release = new ReleaseProductCommand(hold.productId, orderId, reason);
                commandGateway.send(release, (commandMessage, commandResultMessage) -> {
                    if (commandResultMessage.isExceptional()) {
                        Throwable exception = commandResultMessage.exceptionResult();
                        log.warn("Release reservation failed: {}", exception.getMessage());
                    }
                });
            });
            commandGateway.send(
                    new CancelOrderCommand(orderId, reason)
            );
        };

        // If we’re already inside a UoW, side-effects run after commit
        if (CurrentUnitOfWork.isStarted()
                && CurrentUnitOfWork.get().phase().isBefore(UnitOfWork.Phase.AFTER_COMMIT)) {
            CurrentUnitOfWork.get().afterCommit(uow -> action.run());
        } else {
            action.run();
        }
        SagaLifecycle.end();
    }

    private void compensateFailedPayment(String paymentId) {
        log.info("Starting compensation for failed payment: {}", paymentId);
        compensateReservation("payment-failure");
    }

    @SagaEventHandler(associationProperty = "orderId")
    public void on(FailedConfirmReservationEvent event) {
        log.info("Starting compensation for failed reserve confirmation.");
        if (hasReservationShortCircuited()) {
            log.info("PaymentFailedEvent already processed for orderId={}", orderId);
            return;
        }
        this.reservationClosed = true;
        // TODO: revert payment transaction
        compensateFailedPayment(this.paymentId);
    }

    record Hold(UUID productId, Integer quantity) {
    }
}
