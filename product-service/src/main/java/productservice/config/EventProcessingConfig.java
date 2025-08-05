package productservice.config;

import com.thoughtworks.xstream.XStream;
import org.axonframework.config.EventProcessingConfigurer;
import org.axonframework.eventhandling.PropagatingErrorHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
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

    @Bean
    public XStream xStream() {
        XStream xStream = new XStream();
        // Allow all classes in the com.chaintrade.core.model package
        xStream.allowTypesByWildcard(new String[]{
                "com.chaintrade.core.**",
                "productservice.query.**"
        });
        return xStream;
    }
}
