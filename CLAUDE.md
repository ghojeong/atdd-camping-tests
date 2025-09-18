# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is an ATDD (Acceptance Test-Driven Development) testing project for a camping reservation system. The project uses Cucumber with RestAssured and JUnit for testing multiple microservices in a containerized environment.

### Architecture

- **Test Hub**: This repository (`atdd-tests`) serves as the central testing hub
- **Target Services**: kiosk, admin, reservation applications (stored in `repos/` directory when cloned)
- **Infrastructure**: MySQL database and WireMock for external service mocking
- **Network**: Services communicate via Docker network `atdd-net`

## Key Commands

### Testing

```bash
# Run all tests
./gradlew test

# Run specific Cucumber tests
./gradlew test --tests com.camping.tests.RunCucumberTest
```

### Infrastructure Management

```bash
# Start infrastructure and all application services (includes WireMock for payments)
./gradlew allUp

# Alternative: Start services separately
./gradlew infraUp    # Start infrastructure (database)
./gradlew appsUp     # Start application services

# Stop and clean up
./gradlew allDown

# View application logs
./gradlew appsLogs
```

## Development Workflow

### Step-by-Step Implementation Process

The project follows a 3-step implementation approach:

1. **Step 1**: Single service (kiosk) smoke testing
2. **Step 2**: Multi-service integration with shared database
3. **Step 3**: External service mocking with WireMock

### Service Integration Points

- **Database**: MySQL (`atdd-db`) on network `atdd-net`
- **Payment Service**: Mocked via WireMock (`payments-mock:8080`) with mappings in `infra/wiremock/mappings/`
- **Base URLs**: Externalized via environment variables (KIOSK_BASE_URL, ADMIN_BASE_URL, KIOSK_PAYMENT_BASE_URL)

### Repository Structure

```
repos/                          # Cloned service repositories
infra/
├── docker-compose-infra.yml    # Infrastructure services (DB)
├── docker-compose.yml          # Application services
├── dockerfiles/               # Dockerfiles for each service
└── wiremock/mappings/         # WireMock stub definitions
src/test/
├── java/                      # Test step definitions and runners
└── resources/features/        # Cucumber feature files
```

## Environment Variables

Key environment variables for service configuration:

- `KIOSK_BASE_URL`: Base URL for kiosk service (default: http://localhost:18081)
- `ADMIN_BASE_URL`: Base URL for admin service (default: http://localhost:18082)
- `KIOSK_PAYMENT_BASE_URL`: Base URL for payment service (WireMock at http://payments-mock:8080)
- `SPRING_DATASOURCE_URL`: Database connection string
- `SPRING_DATASOURCE_USERNAME`: Database username
- `SPRING_DATASOURCE_PASSWORD`: Database password

### Step 3 Specific Variables

- `KIOSK_ADMIN_BASE_URL`: http://admin:8080 (for container communication)
- `KIOSK_PAYMENT_BASE_URL`: http://payments-mock:8080 (for WireMock integration)

## Dependencies

- Cucumber 7.14.0 for BDD testing
- RestAssured 5.3.2 for API testing
- JUnit Platform for test execution
- MySQL Connector for database access
- Jackson for JSON processing

## Testing Strategy

- **Smoke Tests**: Basic health checks (200 responses)
- **E2E Tests**: End-to-end scenarios across services
- **Payment E2E Tests**: Success/failure scenarios with WireMock stubs
- **Contract Testing**: WireMock stubs for external dependencies
- **Feature Files**: Written in Korean (language: ko)

### Step 3 Testing Flow

1. Infrastructure and services startup: `./gradlew allUp`
2. WireMock stub verification/registration (automatic via mappings)
3. E2E test execution: `./gradlew test`
   - kiosk → admin → DB (existing from Step 2)
   - kiosk → payments(WireMock) → success scenario
   - kiosk → payments(WireMock) → failure scenario
   - kiosk → payments(WireMock) → confirmation success/failure scenarios
4. Cleanup and teardown: `./gradlew allDown`

### WireMock Integration Details

**Service Configuration:**
- **WireMock URL**: `http://payments-mock:8080` (internal Docker network)
- **External Access**: `http://localhost:18090` (for debugging)
- **Mappings**: Auto-loaded from `infra/wiremock/mappings/` directory

**Payment Scenarios:**
- **Payment Creation Success**: `POST /v1/payments` (normal amounts) → 200 OK with approval
- **Payment Creation Failure**: `POST /v1/payments` (amount: 99999) → 400 Bad Request
- **Payment Confirmation Success**: `POST /v1/payments/confirm` (normal amounts) → 200 OK
- **Payment Confirmation Failure**: `POST /v1/payments/confirm` (amount: 99999) → 500 Server Error

**Error Handling Pattern:**
- Kiosk service converts all WireMock 4xx/5xx responses to 200 status with `success: false`
- Business logic errors are represented in response body, not HTTP status codes
- Tests verify success/failure via `success` field rather than HTTP status codes

### Debugging WireMock

```bash
# Check loaded mappings
curl http://localhost:18090/__admin/mappings | jq

# View recent requests
curl http://localhost:18090/__admin/requests | jq

# Test payment creation directly
curl -X POST http://localhost:18090/v1/payments \
  -H "Content-Type: application/json" \
  -d '{"paymentKey":"test","orderId":"test","amount":10000}'
```
