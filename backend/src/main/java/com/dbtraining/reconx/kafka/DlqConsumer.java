package com.dbtraining.reconx.kafka;

import java.time.Instant;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import com.dbtraining.reconx.dto.TradeEvent;
import com.dbtraining.reconx.repository.DlqMessageRepository;
import com.dbtraining.reconx.repository.entity.DlqMessage;

@Component
public class DlqConsumer {

    private static final Logger log = LoggerFactory.getLogger(DlqConsumer.class);

    private final DlqMessageRepository repo;

    public DlqConsumer(DlqMessageRepository repo) {
        this.repo = repo;
    }

    @KafkaListener(
            topics = "trade-events-dlq",
            groupId = "dlq-monitor"
    )
    public void onDlqMessage(ConsumerRecord<String, TradeEvent> record,
                             @Header(value = KafkaHeaders.EXCEPTION_MESSAGE, required = false) String exMsg) {
        TradeEvent event = record.value();
        log.error("DLQ: trade={} eventId={} reason={}",
                event != null ? event.tradeRef() : "UNKNOWN",
                event != null ? event.eventId() : "UNKNOWN",
                exMsg);

        DlqMessage message = new DlqMessage();
        if (event != null) {
            message.setEventId(event.eventId());
        }
        message.setTopic(record.topic().replace("-dlq", ""));
        message.setPayload(event != null ? event.toString() : (record.value() != null ? record.value().toString() : "NULL"));
        message.setErrorReason(exMsg);
        message.setFailedAt(Instant.now());

        repo.save(message);
    }
}