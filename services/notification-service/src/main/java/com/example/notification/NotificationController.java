package com.example.notification;

import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    @PostMapping("/send")
    public Map<String, Object> sendNotification(@RequestBody Map<String, Object> request) {
        String orderId = (String) request.get("orderId");
        String type = (String) request.get("type");

        // Simulate notification sending delay
        try {
            Thread.sleep((long) (Math.random() * 100 + 50));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        String message = switch (type) {
            case "ORDER_CONFIRMED" -> "Your order " + orderId + " has been confirmed!";
            case "ORDER_SHIPPED" -> "Your order " + orderId + " has been shipped!";
            case "ORDER_DELIVERED" -> "Your order " + orderId + " has been delivered!";
            default -> "Order update for " + orderId;
        };

        Map<String, Object> response = new HashMap<>();
        response.put("notificationId", UUID.randomUUID().toString());
        response.put("orderId", orderId);
        response.put("type", type);
        response.put("message", message);
        response.put("channel", "EMAIL");
        response.put("status", "SENT");
        response.put("timestamp", System.currentTimeMillis());

        return response;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        Map<String, String> health = new HashMap<>();
        health.put("service", "notification-service");
        health.put("status", "UP");
        health.put("timestamp", String.valueOf(System.currentTimeMillis()));
        return health;
    }
}
