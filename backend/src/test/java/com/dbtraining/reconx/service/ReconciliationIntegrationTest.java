package com.dbtraining.reconx.service;

import static org.junit.Assert.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.dbtraining.reconx.dto.ReconResult;
import com.dbtraining.reconx.model.EquityTrade;
import com.dbtraining.reconx.model.ReconciliationRule;
import com.dbtraining.reconx.model.TradeType;
import com.dbtraining.reconx.repository.entity.Trade;

@SpringBootTest
@Testcontainers
class ReconciliationIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("reconx")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", postgres::getJdbcUrl);
        r.add("spring.datasource.username", postgres::getUsername);
        r.add("spring.datasource.password", postgres::getPassword);
    }

    @Test
    void containerIsRunning() {
        // sanity: if this passes, all your wiring is correct.
        // The real assertions live in TICKET-ADV045.
    }


    @Test
    void insertedTradesAreReconciledAndPersisted() {
        // given — two matching trades, one in each repo
        Trade internal = new Trade("TRD-INT-1", "CP-1", "SAP.DE",
                new BigDecimal("100"), new BigDecimal("245.50"), LocalDate.now());
        Trade external = new Trade("TRD-INT-1", "CP-1", "SAP.DE",
                new BigDecimal("100"), new BigDecimal("245.50"), LocalDate.now());

        internalTradeRepo.save(internal);
        externalTradeRepo.save(external);

        // when
        reconciliationService.runRecon(
                internalTradeRepo.findAll(),
                externalTradeRepo.findAll());

        // then — exactly one MATCHED row landed in recon_results
        List<ReconResult> persisted = reconResultRepo.findAll();
        assertThat(persisted).hasSize(1);
        assertThat(persisted.get(0).status()).isEqualTo(ReconResult.Status.MATCHED);
        assertThat(persisted.get(0).tradeRef()).isEqualTo("TRD-INT-1");
    }

    @Test
    void testReconcile_emptyInternal_returnsEmpty() {
        assertThat(engine.reconcile(List.of(), List.of(), ReconciliationRule.EXACT)).isEmpty();
    }

    // single internal trade with no external feed -> one BREAK with MISSING_EXTERNAL
    @Test
    void testReconcile_singleInternalNoExternal_returnsBreak() {
        EquityTrade internal = equity("EQU-20260603-EDGE-1", "100.00", "1000");

        List<ReconResult> out = engine.reconcile(List.of(internal), List.of(), ReconciliationRule.EXACT);

        assertThat(out).hasSize(1);
        assertThat(out.get(0).status()).isEqualTo(ReconResult.Status.BREAK);
        assertThat(out.get(0).discrepancyType()).isEqualTo("MISSING_EXTERNAL");
    }

    // all-mismatched -> ReconSummaryCollector reports total == broken, matched == 0
    @Test
    void testReconcile_allMismatched_summaryShowsZeroMatched() {
        List<TradeType> internals = List.of(
                equity("EQU-MM-1", "100.00", "1000"),
                equity("EQU-MM-2", "100.00", "1000"),
                equity("EQU-MM-3", "100.00", "1000"));
        List<TradeType> externals = List.of(
                equity("EQU-MM-1", "200.00", "1000"),
                equity("EQU-MM-2", "200.00", "1000"),
                equity("EQU-MM-3", "200.00", "1000"));

        List<ReconResult> out = engine.reconcile(internals, externals, ReconciliationRule.EXACT);
        ReconSummary summary = out.stream().collect(new ReconSummaryCollector());

        assertThat(summary.total()).isEqualTo(3);
        assertThat(summary.matched()).isEqualTo(0);
        assertThat(summary.broken()).isEqualTo(3);
    }
}