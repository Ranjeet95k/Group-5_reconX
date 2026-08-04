# ReconX Deployment Guide

## 1. Overview

This document describes deployment approaches for the ReconX platform.

Supported environments:

- Local development
- Docker-based deployment
- Production deployment


# 2. Local Deployment

## Prerequisites

Install:

- Java 21
- Maven
- PostgreSQL
- Git


## Steps

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


Application will start with configured local environment settings.


# 3. Docker Deployment

## Prerequisites

Install:

- Docker
- Docker Compose


## Start Services

From project root:

```bash
docker compose up -d
```


## Verify Containers

Check running services:

```bash
docker ps
```


## Stop Services

```bash
docker compose down
```


# 4. Environment Configuration

Configuration values should be managed through environment variables.

Important configurations:

- Database URL
- Database username/password
- JWT secret
- Kafka configuration
- External service URLs


Example:

```properties
SPRING_DATASOURCE_URL=
SPRING_DATASOURCE_USERNAME=
SPRING_DATASOURCE_PASSWORD=
JWT_SECRET=
```


# 5. Production Deployment Notes

Production deployment recommendations:

- Use managed database services
- Store secrets securely
- Enable HTTPS
- Configure monitoring and logging
- Use proper backup strategy
- Perform health checks before release


## Deployment Checklist

Before production release:

- [ ] Database migrations completed
- [ ] Environment variables configured
- [ ] Security settings verified
- [ ] Application health checked
- [ ] Monitoring enabled