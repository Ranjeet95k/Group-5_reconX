package com.dbtraining.reconx.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.dbtraining.reconx.dto.TradeEvent;
import com.dbtraining.reconx.repository.AuditLogRepository;
import com.dbtraining.reconx.repository.entity.AuditLogEntry;

@Component
public class AuditEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(AuditEventConsumer.class);
    private final AuditLogRepository repo;

    public AuditEventConsumer(AuditLogRepository repo) {
        this.repo = repo;
    }

    @KafkaListener(topics = "trade-events", groupId = "audit-service")
    public void onTradeEvent(TradeEvent e) {
        String beforeStr = e.before() != null ? e.before().toString() : null;
        String afterStr = e.after() != null ? e.after().toString() : null;

        repo.save(new AuditLogEntry(
                e.eventId().toString(),
                e.tradeRef(),
                e.eventType().name(),
                e.timestamp(),
                e.actor(),
                beforeStr,
                afterStr));
        log.debug("Audit row persisted for eventId={}", e.eventId());
    }
}