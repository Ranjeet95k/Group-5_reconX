package com.dbtraining.reconx.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EquityTradeTest {

    @Test
    void builder_buildsWhenAllRequiredPresent() {

        EquityTrade trade = EquityTrade.builder()
                .tradeRef(TradeRef.of("SAP-20260603-0001"))
                .instrumentSymbol("SAP.DE")
                .quantity(new BigDecimal("100"))
                .price(new BigDecimal("100"))
                .currency("EUR")
                .side(Side.BUY)
                .tradeDate(LocalDate.of(2026, 6, 3))
                .counterpartyId(1L)
                .build();

        assertThat(trade.tradeRef())
                .isEqualTo(TradeRef.of("SAP-20260603-0001"));

        assertThat(trade.notional().amount())
                .isEqualByComparingTo(new BigDecimal("10000"));

        assertThat(trade.assetClass())
        .isNotNull();
    }


    @Test
    void builder_missingPrice_throws() {

        assertThatThrownBy(() ->
                EquityTrade.builder()
                        .tradeRef(TradeRef.of("SAP-20260603-0001"))
                        .instrumentSymbol("SAP.DE")
                        .quantity(new BigDecimal("100"))
                        .currency("EUR")
                        .side(Side.BUY)
                        .tradeDate(LocalDate.of(2026, 6, 3))
                        .counterpartyId(1L)
                        .build()
        )
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("price");
    }


    @Test
    void equality_byTradeRef() {

        EquityTrade first = sampleEquity("SAP-20260603-0001");

        EquityTrade second = sampleEquity("SAP-20260603-0001");

        EquityTrade third = sampleEquity("SAP-20260603-0002");


        assertThat(first)
                .isEqualTo(second);

        assertThat(first.hashCode())
                .isEqualTo(second.hashCode());

        assertThat(first)
                .isNotEqualTo(third);
    }


    private EquityTrade sampleEquity(String ref) {

        return EquityTrade.builder()
                .tradeRef(TradeRef.of(ref))
                .instrumentSymbol("SAP.DE")
                .quantity(new BigDecimal("100"))
                .price(new BigDecimal("100"))
                .currency("EUR")
                .side(Side.BUY)
                .tradeDate(LocalDate.of(2026, 6, 3))
                .counterpartyId(1L)
                .build();
    }
}