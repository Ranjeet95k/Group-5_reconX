# ADR-003: Use GIN index for JSONB metadata searches

## Status

Accepted

## Context

Instrument metadata stored as JSONB needs efficient searching.
A normal B-tree index is not suitable for searching arbitrary JSONB keys
and values.

## Decision

We will create a PostgreSQL GIN index on the instruments.metadata JSONB
column.

The index will support queries using JSONB operators such as containment
(@>) and key searches.

## Consequences

Positive:
- Faster JSONB lookup operations.
- Supports flexible metadata queries.
- Scales better as metadata volume grows.

Negative:
- Additional storage overhead.
- Index maintenance cost during writes.
- Not every JSONB query can use the index.