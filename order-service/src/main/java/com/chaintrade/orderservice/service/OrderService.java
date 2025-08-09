package com.chaintrade.orderservice.service;

import com.chaintrade.core.model.ProductRestModel;
import com.chaintrade.core.query.FindProductByIdQuery;
import com.chaintrade.orderservice.command.CreateOrderCommand;
import com.chaintrade.orderservice.command.rest.CreateOrderModel;
import com.chaintrade.orderservice.core.data.OrderItem;
import lombok.RequiredArgsConstructor;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final QueryGateway queryGateway;
    private final CommandGateway commandGateway;

    public CompletableFuture<String> placeOrder(CreateOrderModel dto) {
        Map<Integer, OrderItem> mappedItems = IntStream.range(0, dto.getItems().size()).boxed()
                .collect(
                        Collectors.toMap(i -> i, dto.getItems()::get)
                );
        List<CompletableFuture<ProductRestModel>> futures = dto.getItems().stream().map(item ->
                queryGateway.query(
                        new FindProductByIdQuery(item.getProductId()),
                        ResponseTypes.instanceOf(ProductRestModel.class)
                )
        ).toList();

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenApply(v ->
                futures.stream()
                        .map(CompletableFuture::join)
                        .toList()
        ).thenCompose(itemList -> {
            AtomicReference<BigDecimal> total = new AtomicReference<>(BigDecimal.ZERO);
            mappedItems.forEach((index, dtoItem) -> {
                ProductRestModel item = itemList.get(index);
                total.set(total.get().add(item.price().multiply(BigDecimal.valueOf(dtoItem.getQuantity()))));
                dtoItem.setUnitPrice(item.price());
            });
            CreateOrderCommand command = new CreateOrderCommand(
                    UUID.randomUUID().toString(),
                    dto.getCustomerId(),
                    dto.getItems(),
                    total.get(),
                    dto.getShippingAddress()
            );
            return commandGateway.sendAndWait(command);
        });
    }
}
