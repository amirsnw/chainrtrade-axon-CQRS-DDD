package com.chaintrade.orderservice.config;

import com.chaintrade.orderservice.command.interceptors.CreateOrderCommandInterceptor;
import jakarta.annotation.PreDestroy;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.common.Registration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommandInterceptorRegistrar {

    private Registration registration;

    @Autowired
    public void registerOrderCommandInterceptor(ApplicationContext context, CommandBus bus) {
        CreateOrderCommandInterceptor interceptor = context.getBean(CreateOrderCommandInterceptor.class);
        this.registration = bus.registerDispatchInterceptor(interceptor);
    }

    @PreDestroy
    public void cleanUp() {
        if (registration != null) {
            registration.cancel(); // Unregister safely at shutdown
        }
    }
}