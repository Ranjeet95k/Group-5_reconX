# ADR-002: Use JSONB for instrument metadata

## Status

Accepted

## Context

Different asset classes require different metadata fields.
Adding separate columns for every possible attribute would create a large
and frequently changing schema.

PostgreSQL provides JSONB support that allows flexible structured storage
with indexing capabilities.

## Decision

We will store optional instrument-specific attributes in a JSONB column
named metadata on the instruments table.

Examples:
- sector
- country
- dividend yield
- asset-specific properties

## Consequences

Positive:
- Flexible schema for different asset classes.
- Reduced database migrations for new metadata fields.
- PostgreSQL JSONB operators can query structured data.

Negative:
- Validation responsibility moves partly to the application.
- Relational constraints are harder to enforce.