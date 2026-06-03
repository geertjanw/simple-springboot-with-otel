package com.example.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

@RestController
public class HelloController {

    private final Random random = new Random();

    @GetMapping("/hello")
    public Map<String, String> hello() {
        return Map.of(
            "message", "Hello from Dash0 Java Demo!",
            "timestamp", String.valueOf(System.currentTimeMillis())
        );
    }

    @GetMapping("/work")
    public Map<String, Object> work() throws InterruptedException {
        int sleepMs = ThreadLocalRandom.current().nextInt(50, 201);
        Thread.sleep(sleepMs);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "completed");
        response.put("duration_ms", sleepMs);
        response.put("worker_id", "worker-" + random.nextInt(1000));
        response.put("timestamp", System.currentTimeMillis());

        return response;
    }
}
