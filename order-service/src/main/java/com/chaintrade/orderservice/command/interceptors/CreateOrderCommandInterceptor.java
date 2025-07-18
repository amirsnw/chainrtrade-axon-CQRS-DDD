package com.chaintrade.orderservice.command.interceptors;

import com.chaintrade.orderservice.command.CreateOrderCommand;
import com.chaintrade.orderservice.core.data.OrderLookupEntity;
import com.chaintrade.orderservice.core.data.OrderLookupRepository;
import com.chaintrade.orderservice.core.data.OrderStatus;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.messaging.MessageDispatchInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Nonnull;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.function.BiFunction;

@Component
@Slf4j
@RequiredArgsConstructor
public class CreateOrderCommandInterceptor implements MessageDispatchInterceptor<CommandMessage<?>> {

    @Inject
    private final OrderLookupRepository lookupRepository;

    @Nonnull
    @Override
    public BiFunction<Integer, CommandMessage<?>, CommandMessage<?>> handle(
            @Nonnull List<? extends CommandMessage<?>> messages) {
        return (index, command) -> {
            log.info("Intercepted command: " + command.getPayload().toString());
            if (CreateOrderCommand.class.equals(command.getPayloadType())) {
                CreateOrderCommand createOrderCommand = (CreateOrderCommand) command.getPayload();
                if (!StringUtils.hasText(createOrderCommand.getOrderId())) {
                    throw new IllegalArgumentException("Order id is required");
                }
                OrderLookupEntity oldCanceledOrder = lookupRepository.findFirstByCustomerIdAndStatusOrderByDateCreatedDesc(
                        createOrderCommand.getCustomerId(),
                        OrderStatus.CANCELLED
                );
                if (oldCanceledOrder != null) {
                    ZonedDateTime threshold = oldCanceledOrder.getDateCreated().plusMonths(1);
                    if (ZonedDateTime.now().isBefore(threshold)) {
                        throw new IllegalStateException("You cannot create an order that has been cancelled within one last month");
                    }
                }
            }
            return command;
        };
    }
}
