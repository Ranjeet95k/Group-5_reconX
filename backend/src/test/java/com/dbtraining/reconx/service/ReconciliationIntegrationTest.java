package com.dbtraining.reconx.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

import com.dbtraining.reconx.dto.ReconResult;
import com.dbtraining.reconx.model.EquityTrade;
import com.dbtraining.reconx.model.ReconciliationRule;
import com.dbtraining.reconx.model.Side;
import com.dbtraining.reconx.model.TradeRef;

class ReconciliationIntegrationTest {

    private final ReconciliationEngine engine = new ReconciliationEngine();

    @Test
    void matchingTradesAreReportedAsMatched() {
        EquityTrade internal = equity("EQU-20260603-0001", "100.00", "10");
        EquityTrade external = equity("EQU-20260603-0001", "100.00", "10");

        List<ReconResult> results = engine.reconcile(
                List.of(internal),
                List.of(external),
                ReconciliationRule.EXACT
        );

        assertThat(results).hasSize(1);
        assertThat(results.get(0).status()).isEqualTo(ReconResult.Status.MATCHED);
        assertThat(results.get(0).tradeRef()).isEqualTo("EQU-20260603-0001");
    }

    @Test
    void missingExternalTradeIsReportedAsBreak() {
        EquityTrade internal = equity("EQU-20260603-0002", "100.00", "10");

        List<ReconResult> results = engine.reconcile(
                List.of(internal),
                List.of(),
                ReconciliationRule.EXACT
        );

        assertThat(results).hasSize(1);
        assertThat(results.get(0).status()).isEqualTo(ReconResult.Status.BREAK);
        assertThat(results.get(0).discrepancyType()).isEqualTo("MISSING_EXTERNAL");
    }

    @Test
    void emptyInternalListReturnsNoResults() {
        assertThat(engine.reconcile(List.of(), List.of(), ReconciliationRule.EXACT)).isEmpty();
    }

    private EquityTrade equity(String ref, String price, String qty) {
        return EquityTrade.builder()
                .tradeRef(TradeRef.of(ref))
                .instrumentSymbol("SAP.DE")
                .price(new BigDecimal(price))
                .quantity(new BigDecimal(qty))
                .currency("EUR")
                .side(Side.BUY)
                .tradeDate(LocalDate.of(2026, 6, 3))
                .counterpartyId(1L)
                .build();
    }
}