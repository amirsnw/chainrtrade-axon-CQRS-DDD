package com.chaintrade.userservice.query;

import com.chaintrade.core.model.PaymentDetails;
import com.chaintrade.core.model.UserEntity;
import com.chaintrade.core.query.FetchUserPaymentDetailsQuery;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Component;

@Component
public class UserQueryHandler {

    @QueryHandler
    public UserEntity findUser(FetchUserPaymentDetailsQuery query) {
        PaymentDetails paymentDetails = new PaymentDetails(
                "SERGEY KARGOPOLOV",
                "123Card",
                12,
                2030,
                "123"
        );

        return new UserEntity(
                "Sergey",
                "Kargopolov",
                query.userId(),
                paymentDetails
        );
    }
}
