package productservice.command;

import lombok.RequiredArgsConstructor;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;
import productservice.core.data.ProductLookupEntity;
import productservice.core.data.ProductLookupRepository;
import productservice.core.events.ProductCreatedEvent;
import productservice.mapper.ProductMapper;

@Component
@ProcessingGroup("product-group")
@RequiredArgsConstructor
public class ProductLookupEventsHandler {

    private final ProductLookupRepository repository;
    private final ProductMapper mapper;

    @EventHandler
    public void on(ProductCreatedEvent event) {
        ProductLookupEntity productLookupEntity = mapper.toProductLookupEntity(event);
        repository.save(productLookupEntity);
    }
}
