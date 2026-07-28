# ADR-001: Partition trades by trade_date

## Status

Accepted

## Context

The trades table is expected to grow to hundreds of millions of rows.
Querying and maintaining a single large trades table would increase query
latency and make maintenance operations expensive.

Most reconciliation queries filter trades by trade date ranges, making
trade_date the natural partition key.

## Decision

We will partition the trades table by range using the trade_date column.

Monthly partitions will be created so queries can use partition pruning and
only scan the required time periods.

## Consequences

Positive:
- Faster date-range reconciliation queries.
- Easier maintenance and archival of old trade data.
- Improved query planning on large datasets.

Negative:
- Additional operational complexity.
- Partition management scripts are required.
- Cross-partition queries may require additional planning.