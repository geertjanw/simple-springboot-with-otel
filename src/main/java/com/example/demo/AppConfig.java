package com.example.demo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Application-wide bean configuration.
 */
@Configuration
public class AppConfig {

    @Value("${services.http.connect-timeout-ms:2000}")
    private int connectTimeoutMs;

    @Value("${services.http.read-timeout-ms:5000}")
    private int readTimeoutMs;

    /**
     * Shared RestTemplate with explicit timeouts.
     * Without timeouts a hung downstream service will block a thread indefinitely,
     * which is the likely cause of the observed p95 latency spikes on POST /orders.
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .connectTimeout(Duration.ofMillis(connectTimeoutMs))
                .readTimeout(Duration.ofMillis(readTimeoutMs))
                .build();
    }
}
