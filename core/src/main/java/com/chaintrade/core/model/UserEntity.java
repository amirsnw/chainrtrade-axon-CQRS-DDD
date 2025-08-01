package com.chaintrade.core.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserEntity {

    private final String firstName;
    private final String lastName;
    private final String userId;
    private final PaymentDetails paymentDetails;
}
