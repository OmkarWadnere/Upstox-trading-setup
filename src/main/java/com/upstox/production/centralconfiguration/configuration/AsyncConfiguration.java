package com.upstox.production.centralconfiguration.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfiguration {

    @Bean(name = "asyncExecutor")
    public ThreadPoolTaskExecutor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4); // Set the core pool size
        executor.setMaxPoolSize(8); // Set the maximum pool size
        executor.setQueueCapacity(100); // Set the capacity for the ThreadPoolExecutor's BlockingQueue
        executor.setThreadNamePrefix("Async-"); // Set the prefix for thread names
        executor.initialize(); // Initialize the executor
        return executor;
    }
}