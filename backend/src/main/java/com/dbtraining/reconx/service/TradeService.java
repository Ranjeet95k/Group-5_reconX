package com.dbtraining.reconx.service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dbtraining.reconx.dto.TradeEvent;
import com.dbtraining.reconx.dto.TradeRequest;
import com.dbtraining.reconx.exception.TradeNotFoundException;
import com.dbtraining.reconx.kafka.TradeEventProducer;
import com.dbtraining.reconx.observability.TradeMetrics;
import com.dbtraining.reconx.repository.CounterpartyRepository;
import com.dbtraining.reconx.repository.InstrumentRepository;
import com.dbtraining.reconx.repository.TradeRepository;
import static com.dbtraining.reconx.repository.TradeSpecifications.hasStatus;
import static com.dbtraining.reconx.repository.TradeSpecifications.tradeDateBetween;
import com.dbtraining.reconx.repository.entity.Trade;

@Service
@Transactional
public class TradeService {

    private final TradeRepository tradeRepo;
    private final CounterpartyRepository cpRepo;
    private final InstrumentRepository instRepo;
    private final TradeEventProducer events;
    private final TradeMetrics metrics;

    public TradeService(TradeRepository tradeRepo,
                        CounterpartyRepository cpRepo,
                        InstrumentRepository instRepo,
                        TradeEventProducer events,
                        TradeMetrics metrics) {

        this.tradeRepo = tradeRepo;
        this.cpRepo = cpRepo;
        this.instRepo = instRepo;
        this.events = events;
        this.metrics = metrics;
    }

    public Trade create(TradeRequest req, String actor) {
        // TODO(TICKET-ADV064)
        throw new UnsupportedOperationException("TICKET-ADV064");
    }

    public Trade update(Long id, TradeRequest req, String actor) {
        // TODO(TICKET-ADV065)
        throw new UnsupportedOperationException("TICKET-ADV065");
    }

    public Trade updateStatus(Long id, String status, String actor) {
        // TODO(TICKET-ADV066)
        throw new UnsupportedOperationException("TICKET-ADV066");
    }

    public void softDelete(Long id, String actor) {
        Trade t = tradeRepo.findById(id)
                .orElseThrow(() -> new TradeNotFoundException("id=" + id));
        t.softDelete();
        tradeRepo.save(t);
        
        events.publish(new TradeEvent(
                UUID.randomUUID(),
                t.getTradeRef(),
                TradeEvent.EventType.TRADE_CANCELLED,
                Instant.now(),
                actor,
                null,
                null
        ));
    }

    /**
     * TICKET-ADV055
     * TICKET-ADV056
     * Specification based dynamic filtering
     */
    @Transactional(readOnly = true)
    public Page<Trade> list(LocalDate from,
                            LocalDate to,
                            String status,
                            Long counterpartyId,
                            Pageable pageable) {

        Specification<Trade> spec = Specification.allOf(
                tradeDateBetween(from, to),
                hasStatus(status),
                forCounterparty(counterpartyId)
        );

        return tradeRepo.findAll(spec, pageable);
    }

    public static Specification<Trade> forCounterparty(Long counterpartyId) {
        return (root, query, cb) -> {
            if (counterpartyId == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("counterparty").get("id"), counterpartyId);
        };
    }
}