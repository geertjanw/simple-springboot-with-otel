#!/bin/bash

echo "Building all services..."

services=("api-gateway" "order-service" "payment-service" "inventory-service" "notification-service")

for service in "${services[@]}"; do
    echo "Building $service..."
    cd "services/$service"
    ../../mvnw clean package -DskipTests
    cd ../..
done

echo "All services built successfully!"
