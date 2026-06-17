package com.example.inventory;

import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    private final Map<String, Integer> inventory = new ConcurrentHashMap<>();

    public InventoryController() {
        inventory.put("product-001", 100);
        inventory.put("product-002", 50);
        inventory.put("product-003", 200);
    }

    @PostMapping("/check")
    public Map<String, Object> checkInventory(@RequestBody Map<String, Object> request) {
        String productId = (String) request.get("productId");
        Integer quantity = (Integer) request.get("quantity");

        // Simulate inventory check delay
        try {
            Thread.sleep((long) (Math.random() * 50 + 25));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        Integer available = inventory.getOrDefault(productId, 0);
        boolean hasStock = available >= quantity;

        Map<String, Object> response = new HashMap<>();
        response.put("productId", productId);
        response.put("requestedQuantity", quantity);
        response.put("availableQuantity", available);
        response.put("available", hasStock);
        response.put("timestamp", System.currentTimeMillis());

        if (hasStock) {
            inventory.put(productId, available - quantity);
        }

        return response;
    }

    @GetMapping("/{productId}")
    public Map<String, Object> getInventory(@PathVariable String productId) {
        Integer quantity = inventory.getOrDefault(productId, 0);

        Map<String, Object> response = new HashMap<>();
        response.put("productId", productId);
        response.put("quantity", quantity);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        Map<String, String> health = new HashMap<>();
        health.put("service", "inventory-service");
        health.put("status", "UP");
        health.put("timestamp", String.valueOf(System.currentTimeMillis()));
        return health;
    }
}
