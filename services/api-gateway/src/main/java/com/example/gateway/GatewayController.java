package com.example.gateway;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class GatewayController {

    @Autowired
    private RestTemplate restTemplate;

    @PostMapping("/orders")
    public Map<String, Object> createOrder(@RequestBody Map<String, Object> orderRequest) {
        String orderId = UUID.randomUUID().toString();
        String productId = (String) orderRequest.get("productId");
        Integer quantity = (Integer) orderRequest.get("quantity");

        Map<String, Object> response = new HashMap<>();
        response.put("orderId", orderId);
        response.put("message", "Order placed successfully");
        response.put("status", "PROCESSING");

        // Call order service
        try {
            Map<String, Object> orderServiceRequest = new HashMap<>();
            orderServiceRequest.put("orderId", orderId);
            orderServiceRequest.put("productId", productId);
            orderServiceRequest.put("quantity", quantity);

            restTemplate.postForObject(
                "http://localhost:8081/orders",
                orderServiceRequest,
                Map.class
            );
        } catch (Exception e) {
            response.put("error", "Failed to process order: " + e.getMessage());
        }

        return response;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        Map<String, String> health = new HashMap<>();
        health.put("service", "api-gateway");
        health.put("status", "UP");
        health.put("timestamp", String.valueOf(System.currentTimeMillis()));
        return health;
    }
}
