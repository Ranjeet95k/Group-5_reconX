package com.dbtraining.reconx.observability;

import java.util.List;

import org.springframework.stereotype.Component;

import com.dbtraining.reconx.repository.TradeRepository;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;

@Component
public class TradesByStatusGauge {

    public TradesByStatusGauge(MeterRegistry registry, TradeRepository repo) {
        for (String status : List.of("PENDING","MATCHED","UNMATCHED","DISPUTED","CANCELLED")) {
            Gauge.builder("trades_by_status", repo, r -> r.countByStatus(status))
                 .tag("status", status)
                 .description("Trades currently in a given status")
                 .register(registry);
        }
    }
}