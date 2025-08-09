package com.chaintrade.productservice.query;

import com.chaintrade.core.model.ProductRestModel;
import com.chaintrade.core.query.FindProductByIdQuery;
import com.chaintrade.productservice.core.data.ProductRepository;
import com.chaintrade.productservice.mapper.ProductMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProductsQueryHandler {

    private final ProductRepository repository;
    private final ProductMapper mapper;

    @QueryHandler
    public List<ProductRestModel> findProducts(FindProductsQuery findProductsQuery) {
        return repository.findAll()
                .stream()
                .map(mapper::toProductRestModel)
                .collect(Collectors.toList());
    }

    @QueryHandler
    public ProductRestModel findProductById(FindProductByIdQuery findProductByIdQuery) {
        UUID productId = findProductByIdQuery.productId();
        return repository
                .findByProductId(productId)
                .map(mapper::toProductRestModel)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Product with id `%s` not found", productId)));
    }
}
