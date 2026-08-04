# TICKET-ADV159 — Grafana Load Test Screenshots

This folder contains Grafana monitoring screenshots captured during k6 load testing.

## Screenshots Required

### 1. Baseline

System metrics before running the load test.

Captured metrics:
- CPU usage
- Memory usage
- Request throughput
- Application health


### 2. Under Load

Metrics captured while running k6 load test.

Load condition:
- 200 concurrent users
- 60 seconds duration

Captured metrics:
- Request rate
- Response latency
- Error percentage
- JVM metrics


### 3. Recovery

Metrics captured after stopping load.

Captured metrics:
- CPU recovery
- Memory stabilization
- Latency returning to normal