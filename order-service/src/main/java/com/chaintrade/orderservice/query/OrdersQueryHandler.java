package com.chaintrade.orderservice.query;

import com.chaintrade.orderservice.core.data.OrderEntity;
import com.chaintrade.orderservice.core.data.OrderRepository;
import com.chaintrade.orderservice.query.rest.OrderQueryModel;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OrdersQueryHandler {

    private final OrderRepository orderRepository;

    @Getter
    private final ObjectMapper objectMapper;

    @QueryHandler
    public List<OrderQueryModel> findProducts(FindOrdersQuery query) {
        List<OrderEntity> orders = orderRepository.findAll();
        return orders.stream()
                .map(item -> {
                    OrderQueryModel model = new OrderQueryModel();
                    BeanUtils.copyProperties(item, model);
                    try {
                        model.setItems(
                                getObjectMapper().readValue(item.getItems(), new TypeReference<>() {
                                })
                        );
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                    return model;
                }).toList();
    }

}
