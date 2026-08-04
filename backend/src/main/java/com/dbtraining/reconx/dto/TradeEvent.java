package com.dbtraining.reconx.dto;

import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * ============================================================================
 * TradeEvent payload (Kafka envelope)
 *
 * WHAT: Immutable payload published to the trade-events Kafka topic.
 *
 * WHY: Carries before/after snapshots so downstream consumers can rebuild
 * state without querying the database.
 * ============================================================================
 */
public record TradeEvent(
        UUID eventId,
        String tradeRef,
        EventType eventType,
        Instant timestamp,
        String actor,
        JsonNode before,
        JsonNode after
) {

    public enum EventType {
        TRADE_CREATED,
        TRADE_UPDATED,
        TRADE_CANCELLED,
        CREATED,
        UPDATED,
        CANCELLED
    }

    // Constructor overload for 6-arg calls (defaulting actor to "SYSTEM")
    public TradeEvent(
            UUID eventId,
            String tradeRef,
            EventType eventType,
            Instant timestamp,
            JsonNode before,
            JsonNode after
    ) {
        this(eventId, tradeRef, eventType, timestamp, "SYSTEM", before, after);
    }

    // Accessor aliases for backward compatibility with existing consumers/services
    public Instant occurredAt() {
        return timestamp;
    }

    public JsonNode beforeData() {
        return before;
    }

    public JsonNode afterData() {
        return after;
    }

    // Static Factory Methods
    public static TradeEvent created(String tradeRef, JsonNode after) {
        return new TradeEvent(
                UUID.randomUUID(),
                tradeRef,
                EventType.TRADE_CREATED,
                Instant.now(),
                "SYSTEM",
                null,
                after
        );
    }

    public static TradeEvent updated(
            String tradeRef,
            JsonNode before,
            JsonNode after
    ) {
        return new TradeEvent(
                UUID.randomUUID(),
                tradeRef,
                EventType.TRADE_UPDATED,
                Instant.now(),
                "SYSTEM",
                before,
                after
        );
    }

    public static TradeEvent cancelled(
            String tradeRef,
            JsonNode before
    ) {
        return new TradeEvent(
                UUID.randomUUID(),
                tradeRef,
                EventType.TRADE_CANCELLED,
                Instant.now(),
                "SYSTEM",
                before,
                null
        );
    }
}