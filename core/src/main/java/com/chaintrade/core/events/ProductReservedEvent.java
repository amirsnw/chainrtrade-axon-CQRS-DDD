package com.chaintrade.core.events;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class ProductReservedEvent {

    private final UUID productId;
    private final int quantity;
    private final String orderId;
    private final String userId;

}
