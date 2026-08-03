package com.dbtraining.reconx.dto;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.UUID;

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
        JsonNode before,
        JsonNode after

) {

    public enum EventType {
        TRADE_CREATED,
        TRADE_UPDATED,
        TRADE_CANCELLED
    }

    public static TradeEvent created(String tradeRef, JsonNode after) {
        return new TradeEvent(
                UUID.randomUUID(),
                tradeRef,
                EventType.TRADE_CREATED,
                Instant.now(),
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
                before,
                null
        );
    }
}