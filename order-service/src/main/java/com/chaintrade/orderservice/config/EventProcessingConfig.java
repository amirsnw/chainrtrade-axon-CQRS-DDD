package com.chaintrade.orderservice.config;

import com.chaintrade.orderservice.core.errorHandling.OrdersServiceEventsErrorHandler;
import org.axonframework.config.EventProcessingConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EventProcessingConfig {

    @Autowired
    public void configure(EventProcessingConfigurer configurer) {
        configurer.registerListenerInvocationErrorHandler(
                "order-group",
                conf -> new OrdersServiceEventsErrorHandler()
        );
        /*configurer.registerListenerInvocationErrorHandler(
                "order-group",
                conf -> PropagatingErrorHandler.instance()
        );*/
    }
}
