package com.example.order;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private RestTemplate restTemplate;

    @PostMapping
    public Map<String, Object> createOrder(@RequestBody Map<String, Object> orderRequest) {
        String orderId = (String) orderRequest.get("orderId");
        String productId = (String) orderRequest.get("productId");
        Integer quantity = (Integer) orderRequest.get("quantity");

        // Simulate order processing
        try {
            Thread.sleep((long) (Math.random() * 100 + 50));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Check inventory
        Map<String, Object> inventoryRequest = new HashMap<>();
        inventoryRequest.put("productId", productId);
        inventoryRequest.put("quantity", quantity);

        try {
            Map<String, Object> inventoryResponse = restTemplate.postForObject(
                "http://localhost:8082/inventory/check",
                inventoryRequest,
                Map.class
            );

            boolean available = (Boolean) inventoryResponse.get("available");

            if (available) {
                // Process payment
                Map<String, Object> paymentRequest = new HashMap<>();
                paymentRequest.put("orderId", orderId);
                paymentRequest.put("amount", quantity * 29.99);

                Map<String, Object> paymentResponse = restTemplate.postForObject(
                    "http://localhost:8083/payments/process",
                    paymentRequest,
                    Map.class
                );

                String paymentStatus = (String) paymentResponse.get("status");

                if ("SUCCESS".equals(paymentStatus)) {
                    // Send notification
                    Map<String, Object> notificationRequest = new HashMap<>();
                    notificationRequest.put("orderId", orderId);
                    notificationRequest.put("type", "ORDER_CONFIRMED");

                    restTemplate.postForObject(
                        "http://localhost:8084/notifications/send",
                        notificationRequest,
                        Map.class
                    );

                    Map<String, Object> response = new HashMap<>();
                    response.put("orderId", orderId);
                    response.put("status", "CONFIRMED");
                    response.put("message", "Order processed successfully");
                    return response;
                }
            }
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("orderId", orderId);
            response.put("status", "FAILED");
            response.put("error", e.getMessage());
            return response;
        }

        Map<String, Object> response = new HashMap<>();
        response.put("orderId", orderId);
        response.put("status", "FAILED");
        response.put("message", "Order could not be processed");
        return response;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        Map<String, String> health = new HashMap<>();
        health.put("service", "order-service");
        health.put("status", "UP");
        health.put("timestamp", String.valueOf(System.currentTimeMillis()));
        return health;
    }
}
