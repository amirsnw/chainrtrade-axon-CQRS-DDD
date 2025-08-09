package com.chaintrade.productservice.config;

import com.chaintrade.productservice.command.interceptors.CreateProductCommandInterceptor;
import jakarta.annotation.PreDestroy;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.common.Registration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InterceptorsConfiguration {

    private Registration registration;

    @Autowired
    public void registerCreateProductCommandInterceptor(CreateProductCommandInterceptor createProductCommandInterceptor, CommandBus commandBus) {
        this.registration = commandBus.registerDispatchInterceptor(createProductCommandInterceptor);
    }

    @PreDestroy
    public void cleanUp() {
        if (registration != null) {
            registration.cancel(); // Unregister safely at shutdown
        }
    }

}
