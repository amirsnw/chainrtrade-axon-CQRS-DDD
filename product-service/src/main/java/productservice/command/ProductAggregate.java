package productservice.command;

import com.chaintrade.core.commands.ReserveProductCommand;
import com.chaintrade.core.events.ProductReservedEvent;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;
import org.springframework.beans.factory.annotation.Autowired;
import productservice.core.events.ProductCreatedEvent;
import productservice.mapper.ProductMapper;

import java.math.BigDecimal;
import java.util.UUID;

@Aggregate
public class ProductAggregate {

    @AggregateIdentifier
    private UUID productId;
    private String title;
    private BigDecimal price;
    private Integer quantity;

    private transient ProductMapper mapper;

    @Autowired
    public void setMapper(ProductMapper mapper) {
        this.mapper = mapper;
    }

    public ProductAggregate() {
    }

    @CommandHandler
    public ProductAggregate(CreateProductCommand createProductCommand) {

        if (createProductCommand.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price can not be less or equal to zero");
        }

        if (createProductCommand.getTitle() == null || createProductCommand.getTitle().isEmpty()) {
            throw new IllegalArgumentException("Title can not be empty");
        }

        ProductCreatedEvent productCreatedEvent = ProductMapper.INSTANCE.toCreatedEvent(createProductCommand);

        AggregateLifecycle.apply(productCreatedEvent);

        if (productCreatedEvent.getTitle().contains("throw IllegalStateException"))
            throw new IllegalStateException("An error took place in CreateProductCommand @CommandHandler method");
    }

    @CommandHandler
    public void handle(ReserveProductCommand reserveProductCommand) {
        if (quantity < reserveProductCommand.getQuantity()) {
            throw new IllegalArgumentException("Insufficient number of items in stock");
        }
        ProductReservedEvent productReservedEvent = mapper.toEvent(reserveProductCommand);
        AggregateLifecycle.apply(productReservedEvent);
    }

    @EventSourcingHandler
    public void on(ProductCreatedEvent productCreatedEvent) {
        this.productId = productCreatedEvent.getProductId();
        this.title = productCreatedEvent.getTitle();
        this.price = productCreatedEvent.getPrice();
        this.quantity = productCreatedEvent.getQuantity();
    }

    @EventSourcingHandler
    public void on(ProductReservedEvent productReservedEvent) {
        this.quantity -= productReservedEvent.getQuantity();
    }

}
