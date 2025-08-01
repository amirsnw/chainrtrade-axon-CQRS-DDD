package productservice.config;

import jakarta.annotation.PreDestroy;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.common.Registration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import productservice.command.interceptors.CreateProductCommandInterceptor;

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
