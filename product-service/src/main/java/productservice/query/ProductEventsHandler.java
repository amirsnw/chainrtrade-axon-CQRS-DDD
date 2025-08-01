package productservice.query;

import com.chaintrade.core.events.ProductReservedEvent;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.messaging.interceptors.ExceptionHandler;
import org.springframework.stereotype.Component;
import productservice.core.data.ProductEntity;
import productservice.core.data.ProductRepository;
import productservice.core.events.ProductCreatedEvent;
import productservice.mapper.ProductMapper;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
@ProcessingGroup("product-group")
public class ProductEventsHandler {

    private final ProductRepository repository;
    private final ProductMapper mapper;

    //Second way to handle exception in EventHandler
    @ExceptionHandler(resultType = IllegalArgumentException.class)
    public void handle(IllegalArgumentException ex) {
        log.error("IllegalArgumentException in EventHandler (ExceptionHandler): {}:{}",
                ex.getClass().getName(), ex.getMessage());
    }

    @ExceptionHandler(resultType = Exception.class)
    public void handle(Exception ex) throws Exception {
        log.error("Exception in EventHandler (ExceptionHandler): {}:{}",
                ex.getClass().getName(), ex.getMessage());
        throw ex;
    }

    @EventHandler
    public void on(ProductCreatedEvent event) throws Exception {

        ProductEntity productEntity = mapper.toProductEntity(event);
        repository.save(productEntity);

        // One way to handle exception
        try {
            if (productEntity.getTitle().contains("throwInEventHandler IllegalArgumentException (try-catch)"))
                throw new IllegalArgumentException("Some fake code that throws IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            log.error("Exception in EventHandler (try-catch block): {}:{}", ex.getClass().getName(), ex.getMessage());
        }

        if (productEntity.getTitle().contains("throwInEventHandler IllegalArgumentException (ExceptionHandler)"))
            throw new IllegalArgumentException("Some fake code that throws IllegalArgumentException");

        if (productEntity.getTitle().contains("throwInEventHandler Exception (ExceptionHandler)"))
            throw new Exception("Some fake code that throws Common Exception");
    }

    @EventHandler
    public void on(ProductReservedEvent event) {

        UUID productId = event.getProductId();
        ProductEntity productEntity = repository
                .findByProductId(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product with id `" + productId + "` not found"));

        int updatedQuantity = productEntity.getQuantity() - event.getQuantity();
        productEntity.setQuantity(updatedQuantity);
        repository.save(productEntity);

        log.debug("ProductReservedEvent is called for productId: {}", event.getProductId());
    }
}
