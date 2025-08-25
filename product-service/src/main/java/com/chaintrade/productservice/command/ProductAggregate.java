package com.chaintrade.productservice.command;

import com.chaintrade.core.commands.ConfirmProductReservationCommand;
import com.chaintrade.core.commands.ReleaseProductCommand;
import com.chaintrade.core.commands.ReserveProductCommand;
import com.chaintrade.core.events.ProductReservationConfirmedEvent;
import com.chaintrade.core.events.ProductReservationReleasedEvent;
import com.chaintrade.core.events.ProductReservedEvent;
import com.chaintrade.productservice.core.events.ProductCreatedEvent;
import com.chaintrade.productservice.core.events.ProductUpdatedEvent;
import com.chaintrade.productservice.mapper.ProductMapper;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Aggregate(type = "product")
public class ProductAggregate {

    @AggregateIdentifier
    private UUID productId;
    private String title;
    private BigDecimal price;
    private Integer quantity;
    private Map<String, Hold> holds = new HashMap<>();
    private Map<String, UUID> lastClosedWindow = new HashMap<>();

    private transient ProductMapper mapper;

    @CommandHandler
    public void handle(ReserveProductCommand command) {
        expireIfPast(command.orderId());

        // Reject if a newer "window" was already closed for this order
        // This only handle timout/released retry reservations that needs to be fired from
        // reservation's compensating flow (TODO: not implemented yet in OrderSaga)
        UUID closed = lastClosedWindow.get(command.orderId());
        if (closed != null && !closed.equals(command.reservationWindow())) {
            AggregateLifecycle.apply(
                    new ProductReservationReleasedEvent(
                            productId,
                            command.orderId(),
                            0,
                            "window_closed"
                    )
            );
            return;
        }

        // Idempotent re-try within same window
        Hold hold = holds.get(command.orderId());
        if (hold != null && hold.window.equals(command.reservationWindow())) {
            return; // already reserved for this window
        }

        // TTL check (use timestamp provided by caller)
        if (Instant.now().isAfter(command.expiresAt())) {
            AggregateLifecycle.apply(
                    new ProductReservationReleasedEvent(
                            productId,
                            command.orderId(),
                            0,
                            "expired_before_commit"
                    )
            );
            return;
        }

        if (this.quantity < command.quantity()) {
            AggregateLifecycle.apply(
                    new ProductReservationReleasedEvent(
                            productId,
                            command.orderId(),
                            0,
                            "insufficient"
                    )
            );
            return;
        }
        ProductReservedEvent productReservedEvent = mapper.toEvent(command);
        AggregateLifecycle.apply(productReservedEvent);
    }

    @Autowired
    public void setMapper(ProductMapper mapper) {
        this.mapper = mapper;
    }

    public ProductAggregate() {
    }

    @CommandHandler
    public ProductAggregate(CreateProductCommand createProductCommand) {
        validateModification(createProductCommand.price(), createProductCommand.title());
        ProductCreatedEvent productCreatedEvent = ProductMapper.INSTANCE.toCreatedEvent(createProductCommand);
        AggregateLifecycle.apply(productCreatedEvent);

        if (productCreatedEvent.getTitle().contains("throw IllegalStateException"))
            throw new IllegalStateException("An error took place in CreateProductCommand @CommandHandler method");
    }

    @EventSourcingHandler
    public void on(ProductCreatedEvent productCreatedEvent) {
        this.productId = productCreatedEvent.getProductId();
        this.title = productCreatedEvent.getTitle();
        this.price = productCreatedEvent.getPrice();
        this.quantity = productCreatedEvent.getQuantity();
    }

    @CommandHandler
    public void handle(UpdateProductCommand updateProductCommand) {
        validateModification(updateProductCommand.price(), updateProductCommand.title());
        ProductUpdatedEvent productUpdatedEvent = ProductMapper.INSTANCE.toUpdateEvent(updateProductCommand);
        AggregateLifecycle.apply(productUpdatedEvent);
    }

    @EventSourcingHandler
    public void on(ProductUpdatedEvent productUpdatedEvent) {
        this.title = productUpdatedEvent.getTitle();
        this.price = productUpdatedEvent.getPrice();
        this.quantity = productUpdatedEvent.getQuantity();
    }

    @EventSourcingHandler
    public void on(ProductReservedEvent event) {
        this.quantity -= event.quantity();
        holds.put(event.orderId(),
                new Hold(
                        event.quantity(),
                        event.expiresAt(),
                        event.reservationWindow()
                )
        );
    }

    @CommandHandler
    public void handle(ConfirmProductReservationCommand command) {
        expireIfPast(command.orderId());

        Hold hold = holds.remove(command.orderId());
        if (!hold.quantity.equals(command.quantity())) {
            AggregateLifecycle.apply(
                    new ProductReservationReleasedEvent(
                            command.productId(),
                            command.orderId(),
                            command.quantity(),
                            "quantity confirmation mismatch"
                    )
            );
            return;
        }
        ProductReservationConfirmedEvent event = mapper.toEvent(command);
        AggregateLifecycle.apply(event);
    }

    @EventSourcingHandler
    public void on(ProductReservationConfirmedEvent event) {
        holds.remove(event.orderId());
        lastClosedWindow.remove(event.orderId());
    }

    @CommandHandler
    public void handle(ReleaseProductCommand command) {
        int qty = Optional.ofNullable(holds.get(command.orderId()))
                .map(Hold::quantity)
                .orElse(0);

        AggregateLifecycle.apply(
                new ProductReservationReleasedEvent(
                        command.productId(),
                        command.orderId(),
                        qty,
                        command.reason()
                )
        );
    }

    @EventSourcingHandler
    public void on(ProductReservationReleasedEvent event) {
        this.quantity += event.quantity();
        Hold hold = holds.remove(event.orderId());
        lastClosedWindow.put(event.orderId(),
                Optional.ofNullable(hold)
                        .map(h -> h.window)
                        .orElse(UUID.randomUUID())
        );
    }

    private void expireIfPast(String orderId) {
        Hold hold = holds.get(orderId);
        if (hold != null && Instant.now().isAfter(hold.expiresAt())) {
            AggregateLifecycle.apply(
                    new ProductReservationReleasedEvent(
                            productId,
                            orderId,
                            hold.quantity(),
                            "expired"
                    )
            );
        }
    }

    private static void validateModification(BigDecimal createProductCommand, String createProductCommand1) {
        if (createProductCommand.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price can not be less or equal to zero");
        }

        if (createProductCommand1 == null || createProductCommand1.isEmpty()) {
            throw new IllegalArgumentException("Title can not be empty");
        }
    }

    record Hold(Integer quantity, Instant expiresAt, UUID window) {
    }
}
