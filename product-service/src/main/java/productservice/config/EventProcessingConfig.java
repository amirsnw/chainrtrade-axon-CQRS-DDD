package productservice.config;

import org.axonframework.config.EventProcessingConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import productservice.core.errorhandling.ProductServiceEventsErrorHandler;

@Configuration
public class EventProcessingConfig {

    @Autowired
    public void configure(EventProcessingConfigurer configurer) {
        configurer
                .registerListenerInvocationErrorHandler(
                        "product-group",
                        configuration -> new ProductServiceEventsErrorHandler()
                );

//        configurer
//                .registerListenerInvocationErrorHandler(
//                        "product-group",
//                        configuration -> PropagatingErrorHandler.instance()
//                );
    }

}
