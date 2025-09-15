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
# Start infrastructure (database)
docker compose -f infra/docker-compose-infra.yml up -d

# Start application services
docker compose -f infra/docker-compose.yml up -d --build

# Stop and clean up
docker compose -f infra/docker-compose-infra.yml down -v
docker compose -f infra/docker-compose.yml down -v
```

## Development Workflow

### Step-by-Step Implementation Process

The project follows a 3-step implementation approach:

1. **Step 1**: Single service (kiosk) smoke testing
2. **Step 2**: Multi-service integration with shared database
3. **Step 3**: External service mocking with WireMock

### Service Integration Points

- **Database**: MySQL (`atdd-db`) on network `atdd-net`
- **Payment Service**: Mocked via WireMock with mappings in `infra/wiremock/mappings/`
- **Base URLs**: Externalized via environment variables (KIOSK_BASE_URL, ADMIN_BASE_URL, PAYMENTS_BASE_URL)

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

- `KIOSK_BASE_URL`: Base URL for kiosk service
- `ADMIN_BASE_URL`: Base URL for admin service
- `PAYMENTS_BASE_URL`: Base URL for payment service (typically WireMock)
- `SPRING_DATASOURCE_URL`: Database connection string
- `SPRING_DATASOURCE_USERNAME`: Database username
- `SPRING_DATASOURCE_PASSWORD`: Database password

## Dependencies

- Cucumber 7.14.0 for BDD testing
- RestAssured 5.3.2 for API testing
- JUnit Platform for test execution
- MySQL Connector for database access
- Jackson for JSON processing

## Testing Strategy

- **Smoke Tests**: Basic health checks (200 responses)
- **E2E Tests**: End-to-end scenarios across services
- **Contract Testing**: WireMock stubs for external dependencies
- **Feature Files**: Written in Korean (language: ko)
