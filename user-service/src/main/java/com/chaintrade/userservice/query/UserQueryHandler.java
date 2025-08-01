package com.chaintrade.userservice.query;

import com.chaintrade.core.query.FetchUserPaymentDetailsQuery;
import com.chaintrade.core.model.PaymentDetails;
import com.chaintrade.core.model.UserEntity;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Component;

@Component
public class UserQueryHandler {

    @QueryHandler
    public UserEntity findUser(FetchUserPaymentDetailsQuery query) {
        PaymentDetails paymentDetails = PaymentDetails.builder()
                .cardNumber("123Card")
                .cvv("123")
                .name("SERGEY KARGOPOLOV")
                .validUntilMonth(12)
                .validUntilYear(2030)
                .build();

        return UserEntity.builder()
                .firstName("Sergey")
                .lastName("Kargopolov")
                .userId(query.getUserId())
                .paymentDetails(paymentDetails)
                .build();
    }
}
