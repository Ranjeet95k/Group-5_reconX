# ReconX Platform Documentation

## 1. Project Overview

ReconX is an enterprise trade reconciliation platform designed to manage,
process, and reconcile trade data.

The platform provides:

- Trade management
- Reconciliation workflows
- Event-driven processing
- Monitoring and observability


# 2. Architecture Overview

ReconX follows a layered Spring Boot architecture.

Main components:

- Frontend Application
- Backend REST API
- Service Layer
- Repository Layer
- PostgreSQL Database
- Kafka Messaging
- Monitoring Stack


Architecture flow:

User → Frontend → Backend API → Service Layer → Database

Events are processed asynchronously using Kafka.


# 3. Key Features

## Trade Management

- Create and manage trades
- Validate trade information
- Track trade lifecycle


## Reconciliation

- Compare trade records
- Identify mismatches
- Maintain reconciliation results


## Security

- JWT authentication
- Role-based access control
- Protected API endpoints


## Observability

- Application health monitoring
- Metrics collection
- Dashboard visualization


# 4. Setup Instructions


## Prerequisites

Required:

- Java 21
- Maven
- Docker
- Docker Compose


## Local Setup

Clone repository:

```bash
git clone <repository-url>
```

Navigate to backend:

```bash
cd backend
```

Run application:

```bash
./mvnw spring-boot:run
```


## Docker Setup

Start services:

```bash
docker compose up -d
```


Verify:

```bash
docker ps
```


# 5. Usage Guide


## Access Application

Start all required services and access the application through configured endpoints.


## API Usage

The platform exposes REST APIs for:

- Trade creation
- Trade retrieval
- Reconciliation operations


## Monitoring

Monitoring dashboards are available through Grafana.

Metrics are collected using Prometheus.


# 6. Development Guidelines

- Follow layered architecture
- Write unit tests for changes
- Maintain documentation
- Follow secure coding practices
- Review changes before deployment