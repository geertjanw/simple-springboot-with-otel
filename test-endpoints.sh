#!/bin/bash

echo "Testing endpoints..."
echo ""

echo "1. Testing GET /hello"
curl -s http://localhost:8080/hello | jq .
echo ""

echo "2. Testing GET /work (will take 50-200ms)"
curl -s http://localhost:8080/work | jq .
echo ""

echo "3. Generating some traffic (10 requests to each endpoint)..."
for i in {1..10}; do
    curl -s http://localhost:8080/hello > /dev/null
    curl -s http://localhost:8080/work > /dev/null
    echo -n "."
done
echo ""
echo "Done! Check Dash0 for traces, metrics, and logs."
