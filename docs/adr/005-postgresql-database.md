# ADR-005: PostgreSQL Database Decision

## Status

Accepted


## Context

ReconX requires a reliable relational database for storing:

- Trade records
- Reconciliation results
- Audit information
- Application metadata

The database must provide transaction consistency, scalability, and enterprise reliability.


## Decision

PostgreSQL is selected as the primary database for ReconX.

The application will use:

- Spring Data JPA for database access
- Hibernate ORM for entity management
- Liquibase for schema migrations


## Consequences

### Positive

- Strong ACID transaction support
- Mature relational database platform
- Good integration with Spring Boot ecosystem
- Supports indexing and complex queries


### Negative

- Requires database maintenance
- Scaling requires proper database planning


## Alternatives Considered

### MySQL

Rejected because PostgreSQL provides stronger support for advanced relational workloads.


### NoSQL Database

Rejected because reconciliation workflows require relational consistency and transactional guarantees.