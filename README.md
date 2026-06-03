# Dash0 Java Demo

A minimal Spring Boot application instrumented with OpenTelemetry to send traces, metrics, and logs to Dash0.

## Prerequisites

- Java 21
- Maven (or use the included Maven wrapper `./mvnw`)

## Endpoints

- `GET /hello` - Returns a simple greeting with timestamp
- `GET /work` - Simulates work by sleeping 50-200ms and returns a JSON payload with operation details
- `GET /actuator/health` - Health check endpoint
- `GET /actuator/metrics` - Application metrics

## OpenTelemetry Configuration

The application is instrumented using the OpenTelemetry Java agent with the following configuration:

- **Endpoint**: `https://ingress.europe-west4.gcp.dash0.com:4317` (gRPC)
- **Protocol**: `grpc`
- **Authentication**: Bearer token via `Authorization` header
- **Dataset**: `default` via `Dash0-Dataset` header
- **Service Name**: `dash0-java-demo`
- **Service Version**: `1.0.0`
- **Service Namespace**: `demo`

All configuration is stored in `run.sh` (which is gitignored to keep credentials safe).

## Building and Running

### 1. Build the application

```bash
./mvnw clean package
```

### 2. Run with OpenTelemetry instrumentation

```bash
chmod +x run.sh
./run.sh
```

The application will start on port 8080.

### 3. Test the endpoints

In another terminal:

```bash
chmod +x test-endpoints.sh
./test-endpoints.sh
```

Or manually:

```bash
# Test hello endpoint
curl http://localhost:8080/hello

# Test work endpoint
curl http://localhost:8080/work

# Check health
curl http://localhost:8080/actuator/health
```

## What Gets Sent to Dash0

The OpenTelemetry Java agent automatically instruments:

- **Traces**: HTTP requests, internal spans, timing information
- **Metrics**: JVM metrics, HTTP server metrics, custom application metrics
- **Logs**: Application logs with trace correlation

All telemetry follows OpenTelemetry semantic conventions and includes proper resource attributes (service.name, service.version, service.namespace, deployment.environment).

## Troubleshooting

To enable debug logging for the OpenTelemetry agent, uncomment this line in `run.sh`:

```bash
export OTEL_JAVAAGENT_DEBUG=true
```

## Files

- `pom.xml` - Maven project configuration
- `src/main/java/com/example/demo/DemoApplication.java` - Spring Boot main class
- `src/main/java/com/example/demo/HelloController.java` - REST endpoints
- `src/main/resources/application.properties` - Spring Boot configuration
- `opentelemetry-javaagent.jar` - OpenTelemetry Java agent (downloaded)
- `run.sh` - Script to run the app with OTel configuration (gitignored)
- `test-endpoints.sh` - Script to test the endpoints

## References

- [OpenTelemetry Java Instrumentation](https://github.com/open-telemetry/opentelemetry-java-instrumentation)
- [OpenTelemetry Semantic Conventions](https://opentelemetry.io/docs/specs/semconv/)
- [Dash0 Documentation](https://www.dash0.com/docs)
