# Payment System - Multi-Service Architecture

A complete microservices-based payment system with 5 services, instrumented with OpenTelemetry to send traces, metrics, and logs to Dash0.

## Architecture

```
┌─────────────────┐
│   API Gateway   │ :8080
│  (Entry Point)  │
└────────┬────────┘
         │
         v
┌─────────────────┐
│ Order Service   │ :8081
│  (Orchestrator) │
└─┬───────┬───────┘
  │       │
  v       v
┌──────────────┐  ┌──────────────┐
│  Inventory   │  │   Payment    │
│   Service    │  │   Service    │
│    :8082     │  │    :8083     │
└──────────────┘  └──────┬───────┘
                         │
                         v
                  ┌──────────────┐
                  │Notification  │
                  │  Service     │
                  │    :8084     │
                  └──────────────┘
```

## Services

### 1. API Gateway (:8080)
Entry point for all customer requests. Routes requests to appropriate services.

**Endpoints:**
- `POST /api/orders` - Create a new order
- `GET /api/health` - Health check

### 2. Order Service (:8081)
Orchestrates the order fulfillment process by coordinating with other services.

**Endpoints:**
- `POST /orders` - Process an order (internal)
- `GET /orders/health` - Health check

**Flow:**
1. Check inventory availability
2. Process payment
3. Send notification on success

### 3. Inventory Service (:8082)
Manages product inventory and stock availability.

**Endpoints:**
- `POST /inventory/check` - Check and reserve inventory
- `GET /inventory/{productId}` - Get current inventory level
- `GET /inventory/health` - Health check

**Features:**
- In-memory inventory tracking
- Automatic stock reservation on successful checks

### 4. Payment Service (:8083)
Processes payment transactions.

**Endpoints:**
- `POST /payments/process` - Process a payment
- `GET /payments/health` - Health check

**Features:**
- Simulates payment processing delays (100-300ms)
- 10% simulated failure rate for realistic scenarios

### 5. Notification Service (:8084)
Sends notifications to customers.

**Endpoints:**
- `POST /notifications/send` - Send a notification
- `GET /notifications/health` - Health check

**Features:**
- Supports different notification types (ORDER_CONFIRMED, ORDER_SHIPPED, etc.)
- Simulates email delivery

## OpenTelemetry Configuration

All services are instrumented with the OpenTelemetry Java agent:

- **Endpoint**: `https://ingress.europe-west4.gcp.dash0.com:4317`
- **Protocol**: gRPC
- **Dataset**: `Shop` (instead of default)
- **Authentication**: Bearer token
- **Service Namespace**: `payment-system`
- **VCS Repository**: `https://github.com/geertjanw/simple-springboot-with-otel`

Each service automatically reports:
- **Traces**: Distributed traces across all microservices
- **Metrics**: JVM metrics, HTTP metrics, custom application metrics
- **Logs**: Application logs with trace correlation

## Prerequisites

- Java 21
- Maven (or use included Maven wrapper)
- jq (for testing scripts)

## Building

Build all services at once:

```bash
./build-all.sh
```

Or build individually:

```bash
cd services/api-gateway
../../mvnw clean package
```

## Running

### Start All Services

```bash
./run-all-services.sh
```

This starts all 5 services with proper startup sequencing and logs output to `logs/` directory.

### Start Services Individually

```bash
./run-api-gateway.sh
./run-order-service.sh
./run-payment-service.sh
./run-inventory-service.sh
./run-notification-service.sh
```

## Testing

Run the comprehensive test suite:

```bash
./test-payment-system.sh
```

This will:
1. Check health of all services
2. Query inventory levels
3. Create 5 test orders with detailed output
4. Generate load with 20 concurrent orders

### Manual Testing

Create an order:

```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "productId": "product-001",
    "quantity": 2
  }'
```

Check inventory:

```bash
curl http://localhost:8082/inventory/product-001
```

## Service Communication Flow

1. **Customer** → API Gateway: `POST /api/orders`
2. **API Gateway** → Order Service: `POST /orders`
3. **Order Service** → Inventory Service: `POST /inventory/check`
4. **Order Service** → Payment Service: `POST /payments/process`
5. **Order Service** → Notification Service: `POST /notifications/send`

Each step is traced end-to-end with OpenTelemetry, creating distributed traces visible in Dash0.

## Observability in Dash0

Navigate to the **Shop** dataset in Dash0 to see:

- **Service Map**: Visual representation of service dependencies
- **Distributed Traces**: End-to-end request flow across all services
- **Metrics Dashboard**: Performance metrics for each service
- **Logs**: Correlated logs with trace IDs
- **Agent0 Integration**: Direct navigation to source code via `vcs.repository.url.full`

## Simulated Scenarios

The payment system includes realistic scenarios:

- **Payment Failures**: 10% of payments randomly fail
- **Processing Delays**: Variable latency in each service (50-300ms)
- **Inventory Tracking**: Real-time stock level management
- **Error Propagation**: Failed payments result in order failures

## Ports

- 8080: API Gateway
- 8081: Order Service
- 8082: Inventory Service
- 8083: Payment Service
- 8084: Notification Service

## Logs

Service logs are written to `logs/` directory:
- `api-gateway.log`
- `order-service.log`
- `payment-service.log`
- `inventory-service.log`
- `notification-service.log`

## Stopping Services

Press `Ctrl+C` when running `./run-all-services.sh` to stop all services gracefully.

## Development

Each service is a standalone Spring Boot application located in `services/`:
- `services/api-gateway/`
- `services/order-service/`
- `services/payment-service/`
- `services/inventory-service/`
- `services/notification-service/`

Modify source code in `src/main/java/com/example/{service}/` and rebuild with `./build-all.sh`.
