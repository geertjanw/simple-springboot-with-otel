#!/bin/bash

echo "Testing Payment System Microservices"
echo "===================================="
echo ""

# Test 1: Health checks
echo "1. Checking service health..."
echo "   API Gateway:"
curl -s http://localhost:8080/api/health | jq .
echo ""
echo "   Order Service:"
curl -s http://localhost:8081/orders/health | jq .
echo ""
echo "   Inventory Service:"
curl -s http://localhost:8082/inventory/health | jq .
echo ""
echo "   Payment Service:"
curl -s http://localhost:8083/payments/health | jq .
echo ""
echo "   Notification Service:"
curl -s http://localhost:8084/notifications/health | jq .
echo ""

# Test 2: Check inventory
echo "2. Checking inventory for product-001..."
curl -s http://localhost:8082/inventory/product-001 | jq .
echo ""

# Test 3: Create orders
echo "3. Creating test orders..."
for i in {1..5}; do
    echo "   Order #$i:"
    curl -s -X POST http://localhost:8080/api/orders \
        -H "Content-Type: application/json" \
        -d '{
            "productId": "product-001",
            "quantity": 2
        }' | jq .
    echo ""
    sleep 1
done

echo "4. Generating load (20 orders)..."
for i in {1..20}; do
    curl -s -X POST http://localhost:8080/api/orders \
        -H "Content-Type: application/json" \
        -d "{
            \"productId\": \"product-00$((RANDOM % 3 + 1))\",
            \"quantity\": $((RANDOM % 5 + 1))
        }" > /dev/null &
done

wait

echo ""
echo "✓ Testing complete! Check Dash0 for traces and metrics."
echo "  Dataset: Shop"
echo "  Services: api-gateway, order-service, payment-service, inventory-service, notification-service"
