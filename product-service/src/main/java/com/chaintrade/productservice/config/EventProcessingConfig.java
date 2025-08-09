package com.chaintrade.productservice.config;

import org.axonframework.config.EventProcessingConfigurer;
import org.axonframework.eventhandling.PropagatingErrorHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EventProcessingConfig {

    @Autowired
    public void configure(EventProcessingConfigurer configurer) {
        configurer
                .registerListenerInvocationErrorHandler(
                        "product-group",
                        configuration -> PropagatingErrorHandler.instance()
                );
    }
}
