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
import com.dbtraining.reconx.model.TradeType;

class ReconciliationServiceTest {

    @Test
    void testReconcile_returnsMatchedStatusForExactMatches() {
        ReconciliationEngine engine = new ReconciliationEngine();

        List<TradeType> internal = List.of(equity("EQU-20260603-0001", "100.00", "10"));
        List<TradeType> external = List.of(equity("EQU-20260603-0001", "100.00", "10"));

        List<ReconResult> results = engine.reconcile(internal, external, ReconciliationRule.EXACT);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).tradeRef()).isEqualTo("EQU-20260603-0001");
        assertThat(results.get(0).status()).isEqualTo(ReconResult.Status.MATCHED);
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