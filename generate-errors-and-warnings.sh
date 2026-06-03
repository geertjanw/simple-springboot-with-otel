#!/bin/bash

echo "=========================================="
echo "  ERROR & WARNING GENERATOR"
echo "=========================================="
echo ""
echo "Generating errors, warnings, and edge cases..."
echo "Target dataset: Shop"
echo ""

API_GATEWAY="http://localhost:8080"
DURATION=${1:-120}  # Default 2 minutes

echo "Duration: ${DURATION} seconds"
echo ""

# Function to generate various error scenarios
generate_errors() {
    echo "🔴 Generating ERROR scenarios..."

    # 1. Invalid product IDs (404 errors)
    echo "   • Invalid product IDs..."
    for i in {1..20}; do
        curl -s -X POST ${API_GATEWAY}/api/orders \
            -H "Content-Type: application/json" \
            -d "{
                \"productId\": \"invalid-product-${i}\",
                \"quantity\": 1
            }" > /dev/null 2>&1 &
    done

    # 2. Negative quantities (validation errors)
    echo "   • Negative quantities..."
    for i in {1..15}; do
        curl -s -X POST ${API_GATEWAY}/api/orders \
            -H "Content-Type: application/json" \
            -d "{
                \"productId\": \"product-001\",
                \"quantity\": -5
            }" > /dev/null 2>&1 &
    done

    # 3. Massive quantities (out of stock errors)
    echo "   • Excessive quantities (out of stock)..."
    for i in {1..25}; do
        curl -s -X POST ${API_GATEWAY}/api/orders \
            -H "Content-Type: application/json" \
            -d "{
                \"productId\": \"product-001\",
                \"quantity\": 1000
            }" > /dev/null 2>&1 &
    done

    # 4. Malformed JSON (parse errors)
    echo "   • Malformed JSON payloads..."
    for i in {1..10}; do
        curl -s -X POST ${API_GATEWAY}/api/orders \
            -H "Content-Type: application/json" \
            -d "{ invalid json here }" > /dev/null 2>&1 &
    done

    # 5. Missing required fields
    echo "   • Missing required fields..."
    for i in {1..10}; do
        curl -s -X POST ${API_GATEWAY}/api/orders \
            -H "Content-Type: application/json" \
            -d "{
                \"productId\": \"product-001\"
            }" > /dev/null 2>&1 &
    done

    # 6. Wrong HTTP methods (405 errors)
    echo "   • Wrong HTTP methods..."
    for i in {1..10}; do
        curl -s -X PUT ${API_GATEWAY}/api/orders > /dev/null 2>&1 &
        curl -s -X DELETE ${API_GATEWAY}/api/orders > /dev/null 2>&1 &
    done

    # 7. Non-existent endpoints (404 errors)
    echo "   • Non-existent endpoints..."
    for i in {1..15}; do
        curl -s ${API_GATEWAY}/api/nonexistent > /dev/null 2>&1 &
        curl -s http://localhost:8081/orders/invalid/${i} > /dev/null 2>&1 &
    done

    wait
    echo "   ✓ Error generation complete"
}

# Function to generate warning scenarios
generate_warnings() {
    echo "⚠️  Generating WARNING scenarios..."

    # 1. Low stock warnings (requests that will succeed but deplete inventory)
    echo "   • Low stock warnings..."
    for i in {1..30}; do
        curl -s -X POST ${API_GATEWAY}/api/orders \
            -H "Content-Type: application/json" \
            -d "{
                \"productId\": \"product-00$((RANDOM % 3 + 1))\",
                \"quantity\": $((RANDOM % 3 + 3))
            }" > /dev/null 2>&1 &
    done

    # 2. Slow requests (timeout warnings)
    echo "   • Large batch orders (performance warnings)..."
    for i in {1..20}; do
        curl -s -X POST ${API_GATEWAY}/api/orders \
            -H "Content-Type: application/json" \
            -d "{
                \"productId\": \"product-002\",
                \"quantity\": 5
            }" > /dev/null 2>&1 &
    done

    # 3. Rapid sequential requests from same source
    echo "   • Rate limit triggers..."
    for i in {1..50}; do
        curl -s -X POST ${API_GATEWAY}/api/orders \
            -H "Content-Type: application/json" \
            -H "X-Client-ID: aggressive-client-001" \
            -d "{
                \"productId\": \"product-003\",
                \"quantity\": 1
            }" > /dev/null 2>&1
    done &

    wait
    echo "   ✓ Warning generation complete"
}

# Function to generate mixed chaos
generate_chaos() {
    echo "💥 Generating CHAOS (mixed errors, warnings, success)..."

    for i in {1..100}; do
        # Random scenario selector
        case $((RANDOM % 10)) in
            0|1)  # 20% - Success
                curl -s -X POST ${API_GATEWAY}/api/orders \
                    -H "Content-Type: application/json" \
                    -d "{
                        \"productId\": \"product-00$((RANDOM % 3 + 1))\",
                        \"quantity\": 1
                    }" > /dev/null 2>&1 &
                ;;
            2|3)  # 20% - Out of stock
                curl -s -X POST ${API_GATEWAY}/api/orders \
                    -H "Content-Type: application/json" \
                    -d "{
                        \"productId\": \"product-001\",
                        \"quantity\": 100
                    }" > /dev/null 2>&1 &
                ;;
            4)    # 10% - Invalid product
                curl -s -X POST ${API_GATEWAY}/api/orders \
                    -H "Content-Type: application/json" \
                    -d "{
                        \"productId\": \"invalid-${RANDOM}\",
                        \"quantity\": 1
                    }" > /dev/null 2>&1 &
                ;;
            5)    # 10% - Malformed
                curl -s -X POST ${API_GATEWAY}/api/orders \
                    -H "Content-Type: application/json" \
                    -d "bad json" > /dev/null 2>&1 &
                ;;
            6)    # 10% - Wrong method
                curl -s -X PATCH ${API_GATEWAY}/api/orders > /dev/null 2>&1 &
                ;;
            7)    # 10% - Missing fields
                curl -s -X POST ${API_GATEWAY}/api/orders \
                    -H "Content-Type: application/json" \
                    -d "{}" > /dev/null 2>&1 &
                ;;
            8|9)  # 20% - Payment failures (normal flow, but payment declines)
                curl -s -X POST ${API_GATEWAY}/api/orders \
                    -H "Content-Type: application/json" \
                    -d "{
                        \"productId\": \"product-003\",
                        \"quantity\": 1
                    }" > /dev/null 2>&1 &
                ;;
        esac

        # Occasionally pause to create timing variance
        if [ $((i % 20)) -eq 0 ]; then
            wait
            sleep 0.5
        fi
    done

    wait
    echo "   ✓ Chaos generation complete"
}

# Main execution
start_time=$(date +%s)
end_time=$((start_time + DURATION))
iteration=1

while [ $(date +%s) -lt $end_time ]; do
    echo ""
    echo "--- Iteration ${iteration} ---"

    generate_errors
    sleep 2

    generate_warnings
    sleep 2

    generate_chaos
    sleep 3

    elapsed=$(($(date +%s) - start_time))
    remaining=$((DURATION - elapsed))
    echo ""
    echo "⏱️  Time: ${elapsed}s / ${DURATION}s (Remaining: ${remaining}s)"

    iteration=$((iteration + 1))
done

echo ""
echo "=========================================="
echo "  ERROR & WARNING GENERATION COMPLETE!"
echo "=========================================="
echo ""
echo "📊 Generated telemetry includes:"
echo "   • HTTP 400 errors (bad requests)"
echo "   • HTTP 404 errors (not found)"
echo "   • HTTP 405 errors (method not allowed)"
echo "   • JSON parse errors"
echo "   • Validation errors"
echo "   • Out of stock errors"
echo "   • Payment failure errors (~10%)"
echo "   • Low inventory warnings"
echo "   • Performance degradation warnings"
echo "   • Rate limit warnings"
echo ""
echo "🔍 Check Dash0 'Shop' dataset for:"
echo "   - Error rate trends across services"
echo "   - Failed trace patterns"
echo "   - Warning-level log entries"
echo "   - Service health degradation"
echo "   - Error distribution by type"
echo ""
