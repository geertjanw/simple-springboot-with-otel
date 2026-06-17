#!/bin/bash

echo "=========================================="
echo "  MASSIVE LOAD GENERATOR FOR DASH0 SHOP"
echo "=========================================="
echo ""
echo "Generating massive amounts of traces, logs, and metrics..."
echo "Target dataset: Shop"
echo ""

# Configuration
DURATION=${1:-300}  # Default 5 minutes
CONCURRENT_WORKERS=10
API_GATEWAY="http://localhost:8080"

echo "Duration: ${DURATION} seconds"
echo "Concurrent workers: ${CONCURRENT_WORKERS}"
echo ""

# Product catalog
PRODUCTS=("product-001" "product-002" "product-003")

# Function to generate a single order
generate_order() {
    local worker_id=$1
    local request_num=$2

    local product=${PRODUCTS[$((RANDOM % 3))]}
    local quantity=$((RANDOM % 5 + 1))

    curl -s -X POST ${API_GATEWAY}/api/orders \
        -H "Content-Type: application/json" \
        -H "X-Worker-ID: worker-${worker_id}" \
        -H "X-Request-ID: req-${worker_id}-${request_num}" \
        -d "{
            \"productId\": \"${product}\",
            \"quantity\": ${quantity}
        }" > /dev/null 2>&1
}

# Function for continuous order generation
worker_loop() {
    local worker_id=$1
    local end_time=$(($(date +%s) + DURATION))
    local request_count=0

    while [ $(date +%s) -lt $end_time ]; do
        generate_order $worker_id $request_count
        request_count=$((request_count + 1))

        # Variable delay to create realistic traffic patterns
        sleep 0.$((RANDOM % 3))
    done

    echo "Worker ${worker_id} completed ${request_count} requests"
}

# Function to generate burst traffic
burst_traffic() {
    echo "🔥 Generating burst traffic..."
    for i in {1..50}; do
        generate_order "burst" $i &
    done
    wait
    echo "   ✓ Burst complete (50 concurrent requests)"
}

# Function to check service health (generates metrics)
health_check_loop() {
    local end_time=$(($(date +%s) + DURATION))

    while [ $(date +%s) -lt $end_time ]; do
        curl -s ${API_GATEWAY}/api/health > /dev/null 2>&1
        curl -s http://localhost:8081/orders/health > /dev/null 2>&1
        curl -s http://localhost:8082/inventory/health > /dev/null 2>&1
        curl -s http://localhost:8083/payments/health > /dev/null 2>&1
        curl -s http://localhost:8084/notifications/health > /dev/null 2>&1
        sleep 5
    done
}

# Function to query inventory (generates traces and logs)
inventory_query_loop() {
    local end_time=$(($(date +%s) + DURATION))

    while [ $(date +%s) -lt $end_time ]; do
        for product in "${PRODUCTS[@]}"; do
            curl -s http://localhost:8082/inventory/${product} > /dev/null 2>&1
        done
        sleep 3
    done
}

# Start background workers
echo "Starting ${CONCURRENT_WORKERS} concurrent workers..."
for i in $(seq 1 $CONCURRENT_WORKERS); do
    worker_loop $i &
done

# Start health check monitoring
echo "Starting health check monitoring..."
health_check_loop &

# Start inventory queries
echo "Starting inventory queries..."
inventory_query_loop &

# Generate periodic bursts
echo "Starting burst traffic generator..."
burst_count=0
end_time=$(($(date +%s) + DURATION))

while [ $(date +%s) -lt $end_time ]; do
    sleep 20
    burst_traffic
    burst_count=$((burst_count + 1))
done &

echo ""
echo "🚀 Load generation in progress..."
echo "   All workers active!"
echo "   Press Ctrl+C to stop early"
echo ""

# Progress indicator
start_time=$(date +%s)
while [ $(date +%s) -lt $end_time ]; do
    elapsed=$(($(date +%s) - start_time))
    remaining=$((DURATION - elapsed))

    # Calculate approximate requests per second
    echo -ne "   Time: ${elapsed}s / ${DURATION}s | Remaining: ${remaining}s | Bursts: ${burst_count}\r"
    sleep 1
done

echo ""
echo ""
echo "⏱️  Time's up! Waiting for workers to finish..."

# Wait for all background jobs
wait

echo ""
echo "=========================================="
echo "  LOAD GENERATION COMPLETE!"
echo "=========================================="
echo ""
echo "📊 Check Dash0 'Shop' dataset for:"
echo "   • Thousands of distributed traces"
echo "   • Service-to-service call graphs"
echo "   • Payment failures and retries"
echo "   • Inventory depletion patterns"
echo "   • HTTP metrics across all services"
echo "   • JVM metrics (memory, GC, threads)"
echo "   • Application logs with trace correlation"
echo ""
echo "🔍 Recommended queries:"
echo "   - Filter by service: api-gateway, order-service, payment-service, etc."
echo "   - Look for failed payments (10% failure rate)"
echo "   - Trace latency across microservices"
echo "   - Inventory stock changes over time"
echo ""
