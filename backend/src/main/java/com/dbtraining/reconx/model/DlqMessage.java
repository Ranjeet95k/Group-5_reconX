package com.dbtraining.reconx.model;

import com.dbtraining.reconx.dto.TradeEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Converter;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * ============================================================================
 * TICKET-ADV136 — DlqMessage entity (supporting model for DlqConsumer /
 *                 DlqAdminController)
 *
 * WHAT:    Durable record of one quarantined trade event. DlqConsumer persists
 *          one row per DLQ message; DlqAdminController lists them and replays a
 *          single event by eventId.
 * HOW:     JPA entity mapped to the dlq_messages table (009-dlq.xml). The full
 *          TradeEvent payload is stored as JSON via {@link TradeEventJsonConverter}
 *          so the replay endpoint can re-publish the exact original event.
 * WHY:     Without a persisted record the quarantined message is invisible — it
 *          sits on trade-events-dlq but nobody knows which trade failed or why,
 *          and there is nothing to replay from.
 * OBSERVE: After a poison message hits the DLQ, `SELECT * FROM dlq_messages`
 *          shows one row carrying eventId, tradeRef, originalTopic, partition,
 *          offset, payload and reason.
 *
 * NOTE:    No Lombok on this project, so the fluent builder used by DlqConsumer
 *          (`DlqMessage.builder()....build()`) is hand-written below.
 * ============================================================================
 */
@Entity
@Table(name = "dlq_messages")
public class DlqMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, unique = true, length = 36)
    private UUID eventId;

    @Column(name = "trade_ref", length = 30)
    private String tradeRef;

    @Column(name = "original_topic", nullable = false, length = 100)
    private String originalTopic;

    // "partition" and "offset" are SQL reserved words, so the columns are named
    // partition_no / record_offset while the Java fields keep the natural names
    // used by the ConsumerRecord accessors.
    @Column(name = "partition_no", nullable = false)
    private int partition;

    @Column(name = "record_offset", nullable = false)
    private long offset;

    // The original event, stored as JSON. Matches the audit_log pattern:
    // an unbounded VARCHAR keeps H2 (Postgres mode) and Postgres both happy
    // under Hibernate's ddl-auto=validate.
    @Convert(converter = TradeEventJsonConverter.class)
    @Column(name = "payload", columnDefinition = "TEXT")
    private TradeEvent payload;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "first_seen", nullable = false)
    private Instant firstSeen;

    public DlqMessage() {}

    // ---- getters (used by DlqAdminController and by Jackson for GET /dlq) ----
    public Long getId()             { return id; }
    public UUID getEventId()        { return eventId; }
    public String getTradeRef()     { return tradeRef; }
    public String getOriginalTopic(){ return originalTopic; }
    public int getPartition()       { return partition; }
    public long getOffset()         { return offset; }
    public TradeEvent getPayload()  { return payload; }
    public String getReason()       { return reason; }
    public Instant getFirstSeen()   { return firstSeen; }

    // --------------------------------- builder --------------------------------
    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private final DlqMessage m = new DlqMessage();

        public Builder eventId(UUID v)        { m.eventId = v;        return this; }
        public Builder tradeRef(String v)     { m.tradeRef = v;       return this; }
        public Builder originalTopic(String v){ m.originalTopic = v;  return this; }
        public Builder partition(int v)       { m.partition = v;      return this; }
        public Builder offset(long v)         { m.offset = v;         return this; }
        public Builder payload(TradeEvent v)  { m.payload = v;        return this; }
        public Builder reason(String v)       { m.reason = v;         return this; }
        public Builder firstSeen(Instant v)   { m.firstSeen = v;      return this; }

        public DlqMessage build() { return m; }
    }

    /**
     * Serialises the TradeEvent payload to/from a JSON string column. Uses a
     * self-contained ObjectMapper (with JavaTimeModule for the Instant field)
     * so it does not depend on Spring's bean wiring — JPA instantiates
     * AttributeConverters directly.
     */
    @Converter
    public static class TradeEventJsonConverter
            implements AttributeConverter<TradeEvent, String> {

        private static final ObjectMapper MAPPER =
                new ObjectMapper().registerModule(new JavaTimeModule());

        @Override
        public String convertToDatabaseColumn(TradeEvent event) {
            if (event == null) return null;
            try {
                return MAPPER.writeValueAsString(event);
            } catch (Exception e) {
                throw new IllegalStateException("Failed to serialise TradeEvent for DLQ", e);
            }
        }

        @Override
        public TradeEvent convertToEntityAttribute(String json) {
            if (json == null || json.isBlank()) return null;
            try {
                return MAPPER.readValue(json, TradeEvent.class);
            } catch (Exception e) {
                throw new IllegalStateException("Failed to deserialise TradeEvent from DLQ", e);
            }
        }
    }
}