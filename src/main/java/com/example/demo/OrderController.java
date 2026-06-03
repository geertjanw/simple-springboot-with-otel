package com.example.demo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Handles order creation (POST /orders) and the lightweight liveness probe
 * (GET /orders/health).
 *
 * Downstream service base URLs are injected from application.properties so that
 * no hostname or port is ever hardcoded in source.  In Kubernetes the values
 * default to the cluster-internal DNS names; for local development override them
 * with environment variables:
 *
 *   SERVICES_PAYMENT_URL=http://localhost:8083
 *   SERVICES_INVENTORY_URL=http://localhost:8082
 */
@RestController
@RequestMapping("/orders")
public class OrderController {

    private final RestTemplate restTemplate;

    @Value("${services.payment.url}")
    private String paymentServiceUrl;

    @Value("${services.inventory.url}")
    private String inventoryServiceUrl;

    public OrderController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Lightweight liveness probe.  Must NOT call any downstream service —
     * a slow or unavailable dependency must not cause the pod to be restarted.
     */
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }

    /**
     * Create an order.
     *
     * <ol>
     *   <li>Check inventory availability.</li>
     *   <li>Process payment.</li>
     *   <li>Return the created order summary.</li>
     * </ol>
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createOrder(@RequestBody Map<String, Object> request) {
        String orderId = UUID.randomUUID().toString();

        // --- 1. Inventory check ---
        Map<String, Object> inventoryRequest = new HashMap<>();
        inventoryRequest.put("productId", request.get("productId"));
        inventoryRequest.put("quantity", request.get("quantity"));

        ResponseEntity<Map> inventoryResponse;
        try {
            inventoryResponse = restTemplate.postForEntity(
                    inventoryServiceUrl + "/inventory/check",
                    inventoryRequest,
                    Map.class
            );
        } catch (ResourceAccessException e) {
            return ResponseEntity.status(503).body(Map.of(
                    "error", "inventory-service unavailable",
                    "detail", e.getMessage()
            ));
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of(
                    "error", "inventory check failed",
                    "detail", e.getResponseBodyAsString()
            ));
        }

        Boolean available = (Boolean) inventoryResponse.getBody().getOrDefault("available", false);
        if (!available) {
            return ResponseEntity.status(409).body(Map.of(
                    "error", "insufficient inventory",
                    "productId", String.valueOf(request.get("productId"))
            ));
        }

        // --- 2. Payment ---
        Map<String, Object> paymentRequest = new HashMap<>();
        paymentRequest.put("orderId", orderId);
        paymentRequest.put("amount", request.get("amount"));
        paymentRequest.put("currency", request.getOrDefault("currency", "USD"));

        ResponseEntity<Map> paymentResponse;
        try {
            paymentResponse = restTemplate.postForEntity(
                    paymentServiceUrl + "/payments/process",
                    paymentRequest,
                    Map.class
            );
        } catch (ResourceAccessException e) {
            return ResponseEntity.status(503).body(Map.of(
                    "error", "payment-service unavailable",
                    "detail", e.getMessage()
            ));
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of(
                    "error", "payment processing failed",
                    "detail", e.getResponseBodyAsString()
            ));
        }

        // --- 3. Success response ---
        Map<String, Object> order = new HashMap<>();
        order.put("orderId", orderId);
        order.put("status", "CREATED");
        order.put("productId", request.get("productId"));
        order.put("quantity", request.get("quantity"));
        order.put("amount", request.get("amount"));
        order.put("paymentId", paymentResponse.getBody().get("paymentId"));

        return ResponseEntity.status(201).body(order);
    }
}
