package com.chaintrade.productservice.command;

import com.chaintrade.core.commands.ReleaseProductCommand;
import com.chaintrade.core.commands.ReserveProductCommand;
import com.chaintrade.core.events.ProductReleaseReservedEvent;
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
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Aggregate(type = "product")
public class ProductAggregate {

    @AggregateIdentifier
    private UUID productId;
    private String title;
    private BigDecimal price;
    private Integer quantity;
    private Map<String, Integer> holds = new HashMap<>();

    private transient ProductMapper mapper;

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

    @CommandHandler
    public void handle(ReserveProductCommand command) {
        Integer hold = holds.get(command.orderId());
        if (hold == null) {
            if (this.quantity < command.quantity()) {
                throw new IllegalArgumentException("Insufficient number of items in stock");
            }
            ProductReservedEvent productReservedEvent = mapper.toEvent(command);
            AggregateLifecycle.apply(productReservedEvent);
        }
    }

    @EventSourcingHandler
    public void on(ProductReservedEvent productReservedEvent) {
        this.quantity -= productReservedEvent.quantity();
        holds.put(productReservedEvent.orderId(), productReservedEvent.quantity());
    }

    @CommandHandler
    public void handle(ReleaseProductCommand command) {
        Integer hold = holds.get(command.orderId());
        if (hold != null) {
            ProductReleaseReservedEvent event = mapper.toEvent(command);
            AggregateLifecycle.apply(event);
        }
    }

    @EventSourcingHandler
    public void on(ProductReleaseReservedEvent event) {
        this.quantity += holds.get(event.orderId());
        holds.remove(event.orderId());
    }

    private static void validateModification(BigDecimal createProductCommand, String createProductCommand1) {
        if (createProductCommand.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price can not be less or equal to zero");
        }

        if (createProductCommand1 == null || createProductCommand1.isEmpty()) {
            throw new IllegalArgumentException("Title can not be empty");
        }
    }
}
