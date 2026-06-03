package com.example.payment;

import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    @PostMapping("/process")
    public Map<String, Object> processPayment(@RequestBody Map<String, Object> paymentRequest) {
        String orderId = (String) paymentRequest.get("orderId");
        Double amount = ((Number) paymentRequest.get("amount")).doubleValue();

        // Simulate payment processing delay
        try {
            Thread.sleep((long) (Math.random() * 200 + 100));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Simulate 10% payment failure rate
        boolean success = Math.random() > 0.1;

        Map<String, Object> response = new HashMap<>();
        response.put("orderId", orderId);
        response.put("transactionId", UUID.randomUUID().toString());
        response.put("amount", amount);
        response.put("status", success ? "SUCCESS" : "FAILED");
        response.put("timestamp", System.currentTimeMillis());

        if (!success) {
            response.put("errorCode", "PAYMENT_DECLINED");
            response.put("errorMessage", "Insufficient funds or payment declined");
        }

        return response;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        Map<String, String> health = new HashMap<>();
        health.put("service", "payment-service");
        health.put("status", "UP");
        health.put("timestamp", String.valueOf(System.currentTimeMillis()));
        return health;
    }
}
