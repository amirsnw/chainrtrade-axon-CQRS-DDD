package com.chaintrade.core.config;

import org.axonframework.deadline.DeadlineManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AxonDeadlineConfig {

    @Bean
    public DeadlineManager deadlineManager(
            org.axonframework.config.Configuration axonConfiguration,
            org.axonframework.common.transaction.TransactionManager transactionManager
    ) {
        return org.axonframework.deadline.SimpleDeadlineManager.builder()
                .scopeAwareProvider(axonConfiguration.scopeAwareProvider())
                .transactionManager(transactionManager)
                .build();
    }
}