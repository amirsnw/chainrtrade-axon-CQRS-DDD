package com.chaintrade.productservice.command;

import com.chaintrade.productservice.core.data.ProductLookupEntity;
import com.chaintrade.productservice.core.data.ProductLookupRepository;
import com.chaintrade.productservice.core.events.ProductCreatedEvent;
import com.chaintrade.productservice.core.events.ProductUpdatedEvent;
import com.chaintrade.productservice.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

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

    @EventHandler
    public void on(ProductUpdatedEvent event) {
        ProductLookupEntity productLookupEntity = mapper.toProductLookupEntity(event);
        repository.save(productLookupEntity);
    }
}
