package com.dbtraining.reconx.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.dbtraining.reconx.dto.ReconResult;
import com.dbtraining.reconx.model.BondTrade;
import com.dbtraining.reconx.model.DerivativeTrade;
import com.dbtraining.reconx.model.EquityTrade;
import com.dbtraining.reconx.model.FXTrade;
import com.dbtraining.reconx.model.ReconciliationRule;
import com.dbtraining.reconx.model.TradeType;

import io.micrometer.core.annotation.Timed;

/**
 * ============================================================================
 * TICKET-ADV033 — ReconciliationEngine using Streams (parallel matching)
 *
 * WHAT:
 * Compares internal trades against external trades and returns
 * MATCHED or BREAK results.
 *
 * HOW:
 * External trades are indexed by trade reference and internal trades
 * are processed using streams.
 *
 * WHY:
 * Central reconciliation service used by the application.
 * ============================================================================
 */
@Service
public class ReconciliationEngine {

    private final ExecutorService executor =
            Executors.newFixedThreadPool(
                    Runtime.getRuntime().availableProcessors()
            );

    @Timed(
            value = "reconciliation.duration",
            description = "Wall time of reconcile()",
            percentiles = {0.5, 0.95, 0.99},
            histogram = true
    )
    public List<ReconResult> reconcile(
            List<TradeType> internal,
            List<TradeType> external,
            ReconciliationRule rule
    ) {

        if (internal == null || internal.isEmpty()) {
            return List.of();
        }

        List<TradeType> ext =
                external == null ? List.of() : external;

        Map<String, TradeType> externalByRef =
                ext.stream()
                        .collect(Collectors.toMap(
                                t -> t.tradeRef().value(),
                                Function.identity(),
                                (a, b) -> a
                        ));

        return internal.parallelStream()
                .map(in ->
                        matchOne(
                                in,
                                externalByRef.get(in.tradeRef().value()),
                                rule
                        )
                )
                .toList();
    }


    /**
     * TICKET-ADV037
     *
     * Reconciles trades grouped by counterparty concurrently.
     */
    public CompletableFuture<List<ReconResult>> reconcileByCounterparty(
            Map<Long, List<TradeType>> internalByCp,
            Map<Long, List<TradeType>> externalByCp,
            ReconciliationRule rule
    ) {

        List<CompletableFuture<List<ReconResult>>> futures =
                new ArrayList<>();

        for (Long cp : internalByCp.keySet()) {

            futures.add(
                    CompletableFuture.supplyAsync(
                            () -> reconcile(
                                    internalByCp.get(cp),
                                    externalByCp.getOrDefault(
                                            cp,
                                            List.of()
                                    ),
                                    rule
                            ),
                            executor
                    )
            );
        }

        return CompletableFuture
                .allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v ->
                        futures.stream()
                                .flatMap(f -> f.join().stream())
                                .toList()
                );
    }

    public List<ReconResult> reconcile(List<TradeType> internal,List<TradeType> external,ReconciliationRule rule) {
        if (internal == null || internal.isEmpty()) return List.of();

        Map<String, TradeType> externalByRef = (external == null ? List.<TradeType>of() : external)
                .stream()
                .collect(Collectors.toMap(t -> t.tradeRef().value(), Function.identity(), (a, b) -> a));

        return internal.parallelStream()
                .map(in -> matchOne(in, externalByRef.get(in.tradeRef().value()), rule))
                .toList();
        }


    private ReconResult matchOne(
            TradeType internal,
            TradeType external,
            ReconciliationRule rule
    ) {

        String ref = internal.tradeRef().value();

        if (external == null) {
            return ReconResult.breakResult(
                    ref,
                    "MISSING_EXTERNAL",
                    "no external trade found for " + ref
            );
        }

        BigDecimal[] in = priceQty(internal);
        BigDecimal[] out = priceQty(external);

        if (rule.matches(
                in[0],
                in[1],
                out[0],
                out[1]
        )) {
            return ReconResult.matched(ref);
        }

        return ReconResult.breakResult(
                ref,
                "VALUE_MISMATCH",
                "internal price=%s qty=%s vs external price=%s qty=%s"
                        .formatted(
                                in[0],
                                in[1],
                                out[0],
                                out[1]
                        )
        );
    }


    /**
     * TICKET-ADV018
     *
     * Exhaustive switch over sealed TradeType hierarchy.
     */
    private BigDecimal[] priceQty(TradeType trade) {

        return switch (trade) {

            case EquityTrade e ->
                    new BigDecimal[]{
                            e.price(),
                            e.quantity()
                    };

            case FXTrade fx ->
                    new BigDecimal[]{
                            fx.fxRate(),
                            fx.notionalCcy1()
                    };

            case BondTrade b ->
                    new BigDecimal[]{
                            b.couponRate(),
                            b.faceValue()
                    };

            case DerivativeTrade d ->
                    new BigDecimal[]{
                            d.strike(),
                            d.quantity()
                    };
        };
    }

    
}