package com.chaintrade.productservice.command.interceptors;

import com.chaintrade.productservice.command.CreateProductCommand;
import com.chaintrade.productservice.core.data.ProductLookupEntity;
import com.chaintrade.productservice.core.data.ProductLookupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.messaging.MessageDispatchInterceptor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

@Component
@Slf4j
@RequiredArgsConstructor
public class CreateProductCommandInterceptor implements MessageDispatchInterceptor<CommandMessage<?>> {

    static final String PRODUCT_EXISTS_PATTERN = "Product with title `%s` or product ID `%s` already exists";
    private final ProductLookupRepository lookupRepository;

    @Override
    public BiFunction<Integer, CommandMessage<?>, CommandMessage<?>> handle(List<? extends CommandMessage<?>> messages) {
        return (index, command) -> {

            log.debug("Intercepted command type: {}", command.getPayloadType());
            if (CreateProductCommand.class.equals(command.getPayloadType())) {
                CreateProductCommand createProductCommand = (CreateProductCommand) command.getPayload();
                Optional<ProductLookupEntity> productLookupEntity = lookupRepository.findByProductIdOrTitle(
                        createProductCommand.productId(),
                        createProductCommand.title()
                );

                if (productLookupEntity.isPresent()) {
                    throw new IllegalArgumentException(
                            String.format(PRODUCT_EXISTS_PATTERN,
                                    createProductCommand.title(),
                                    createProductCommand.productId()
                            )
                    );
                }

                if (createProductCommand.title().contains("`")) {
                    throw new IllegalArgumentException("Title must not contain ` symbol (fake validation)");
                }
            }
            return command;
        };
    }
}
