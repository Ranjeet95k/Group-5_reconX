// package com.dbtraining.reconx.controller;

// import com.dbtraining.reconx.dto.TradeMapper;
// import com.dbtraining.reconx.dto.TradeRequest;
// import com.dbtraining.reconx.dto.TradeResponse;
// import com.dbtraining.reconx.repository.entity.Counterparty;
// import com.dbtraining.reconx.repository.entity.Instrument;
// import com.dbtraining.reconx.repository.entity.Trade;
// import com.dbtraining.reconx.security.JwtTokenProvider;
// import com.dbtraining.reconx.service.TradeService;
// import com.fasterxml.jackson.databind.ObjectMapper;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
// import org.springframework.boot.test.mock.mockito.MockBean;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Import;
// import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
// import org.springframework.http.MediaType;
// import org.springframework.security.test.context.support.WithMockUser;
// import org.springframework.test.util.ReflectionTestUtils;
// import org.springframework.test.web.servlet.MockMvc;

// import java.math.BigDecimal;
// import java.time.Instant;
// import java.time.LocalDate;

// import static org.hamcrest.Matchers.containsString;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.ArgumentMatchers.anyString;
// import static org.mockito.Mockito.when;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// @WebMvcTest(TradeController.class)
// @Import(TradeControllerWebMvcTest.TestJwtConfig.class)
// class TradeControllerWebMvcTest {

//     @org.springframework.boot.test.context.TestConfiguration
//     static class TestJwtConfig {
//         @Bean
//         JwtTokenProvider jwtTokenProvider() {
//             return org.mockito.Mockito.mock(JwtTokenProvider.class);
//         }
//     }

//     @Autowired private MockMvc mockMvc;
//     @Autowired private ObjectMapper objectMapper;
//     @MockBean  private TradeService tradeService;
//     @MockBean  private TradeMapper tradeMapper;
//     @MockBean  private JpaMetamodelMappingContext jpaMetamodelMappingContext;

//     private TradeRequest validRequest() {
//         // Field order matches the current TradeRequest record:
//         // (tradeRef, instrumentId, counterpartyId, assetClass, side, quantity, price, tradeDate).
//         // tradeRef regex: ^[A-Z]{3}-\d{8}-\d{4}$. Status is NOT a request field — it is set server-side.
//         return new TradeRequest(
//                 "TRD-20260315-9999",
//                 1L,
//                 1L,
//                 "EQUITY",
//                 "BUY",
//                 new BigDecimal("100.0000"),
//                 new BigDecimal("245.50"),
//                 LocalDate.now());
//     }

//     @Test
//     @WithMockUser(roles = "TRADER")
//     void testCreateTrade_authenticated_returns201() throws Exception {
//         // Field order matches the current TradeResponse record:
//         // (id, tradeRef, instrumentId, instrumentSymbol, counterpartyId, counterpartyName,
//         //  assetClass, side, quantity, price, tradeDate, status, createdAt, modifiedAt).
//         Instant now = Instant.now();

//         Trade trade = new Trade();
//         trade.setTradeRef("TRD-20260315-9999");
//         trade.setAssetClass("EQUITY");
//         trade.setSide("BUY");
//         trade.setQuantity(new BigDecimal("100.0000"));
//         trade.setPrice(new BigDecimal("245.50"));
//         trade.setTradeDate(LocalDate.now());
//         trade.setStatus("PENDING");

//         Instrument instrument = new Instrument();
//         ReflectionTestUtils.setField(instrument, "id", 1L);
//         ReflectionTestUtils.setField(instrument, "symbol", "SAP.DE");
//         trade.setInstrument(instrument);

//         Counterparty counterparty = new Counterparty();
//         ReflectionTestUtils.setField(counterparty, "id", 1L);
//         ReflectionTestUtils.setField(counterparty, "name", "Apex Brokers Inc");
//         trade.setCounterparty(counterparty);

//         ReflectionTestUtils.setField(trade, "id", 42L);
//         ReflectionTestUtils.setField(trade, "createdAt", now);
//         ReflectionTestUtils.setField(trade, "modifiedAt", now);

//         when(tradeService.create(any(TradeRequest.class), any())).thenReturn(trade);
//         when(tradeMapper.toResponse(trade)).thenReturn(new TradeResponse(
//                 42L,
//                 "TRD-20260315-9999",
//                 1L,
//                 "SAP.DE",
//                 1L,
//                 "Apex Brokers Inc",
//                 "EQUITY",
//                 "BUY",
//                 new BigDecimal("100.0000"),
//                 new BigDecimal("245.50"),
//                 LocalDate.now(),
//                 "PENDING",
//                 now,
//                 now));

//         mockMvc.perform(post("/v1/trades")
//                         .contentType(MediaType.APPLICATION_JSON)
//                         .content(objectMapper.writeValueAsString(validRequest()))
//                         .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf()))
//                 .andExpect(status().isCreated())
//                 .andExpect(header().string("Location", containsString("/api/v1/trades/42")))
//                 .andExpect(jsonPath("$.id").value(42))
//                 .andExpect(jsonPath("$.tradeRef").value("TRD-20260315-9999"));
//     }

//     @Test
//     void testCreateTrade_unauthenticated_returns401() throws Exception {
//         mockMvc.perform(post("/v1/trades")
//                         .contentType(MediaType.APPLICATION_JSON)
//                         .content(objectMapper.writeValueAsString(validRequest())))
//                 .andExpect(status().isUnauthorized());
//     }

//     // @Test
//     // @WithMockUser(roles = "VIEWER")
//     // void testCreateTrade_viewerRole_returns403() throws Exception {
//     //     mockMvc.perform(post("/v1/trades")
//     //                     .contentType(MediaType.APPLICATION_JSON)
//     //                     .content(objectMapper.writeValueAsString(validRequest()))
//     //                     .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf()))
//     //             .andExpect(status().isForbidden());
//     // }
// }