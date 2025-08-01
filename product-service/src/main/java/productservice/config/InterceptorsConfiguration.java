package productservice.config;

import org.axonframework.commandhandling.CommandBus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import productservice.command.interceptors.CreateProductCommandInterceptor;

@Configuration
public class InterceptorsConfiguration {

    @Autowired
    public void registerCreateProductCommandInterceptor(CreateProductCommandInterceptor createProductCommandInterceptor, CommandBus commandBus) {
        commandBus.registerDispatchInterceptor(createProductCommandInterceptor);
    }

}
