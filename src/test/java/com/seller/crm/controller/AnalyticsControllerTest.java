package com.seller.crm.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.seller.crm.dto.analytics.BestPeriodResponse;
import com.seller.crm.dto.analytics.SellerAggregateResponse;
import com.seller.crm.dto.analytics.TopSellerResponse;
import com.seller.crm.entity.PeriodType;
import com.seller.crm.service.AnalyticsService;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AnalyticsController.class)
class AnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AnalyticsService analyticsService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    void getTopSeller_shouldReturn200_whenRowsFound() throws Exception {
        TopSellerResponse response = new TopSellerResponse(
            7L, "Egor", new BigDecimal("999.99"), PeriodType.MONTH,
            LocalDateTime.of(2026, 5, 1, 0, 0),
            LocalDateTime.of(2026, 6, 1, 0, 0)
        );
        when(analyticsService.findTopSeller(eq(PeriodType.MONTH), eq(LocalDate.of(2026, 5, 15))))
            .thenReturn(Optional.of(response));

        mockMvc.perform(get("/api/v1/analytics/top-seller")
                .param("period", "MONTH")
                .param("date", "2026-05-15"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.sellerId").value(7))
            .andExpect(jsonPath("$.sellerName").value("Egor"))
            .andExpect(jsonPath("$.period").value("MONTH"));
    }

    @Test
    void getTopSeller_shouldReturn204_whenNoData() throws Exception {
        when(analyticsService.findTopSeller(any(), any())).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/analytics/top-seller")
                .param("period", "YEAR")
                .param("date", "2030-01-01"))
            .andExpect(status().isNoContent());
    }

    @Test
    void getSellersBelowThreshold_shouldReturn200_withArray() throws Exception {
        List<SellerAggregateResponse> response = List.of(
            new SellerAggregateResponse(1L, "Egor", new BigDecimal("500")),
            new SellerAggregateResponse(2L, "Ivan", new BigDecimal("750"))
        );
        when(analyticsService.findSellersBelowThreshold(any(), any(), any()))
            .thenReturn(response);

        mockMvc.perform(get("/api/v1/analytics/sellers/below")
                .param("from", "2026-05-01T00:00:00")
                .param("to", "2026-06-01T00:00:00")
                .param("threshold", "1000"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].sellerId").value(1))
            .andExpect(jsonPath("$[1].sellerId").value(2));
    }

    @Test
    void getBestPeriod_shouldReturn404_whenSellerMissing() throws Exception {
        when(analyticsService.findBestPeriod(eq(99L), any(Integer.class)))
            .thenThrow(new EntityNotFoundException("Seller with id 99 not found"));

        mockMvc.perform(get("/api/v1/analytics/sellers/99/best-period")
                .param("windowDays", "7"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void getTopSeller_shouldReturn400_whenPeriodInvalid() throws Exception {
        mockMvc.perform(get("/api/v1/analytics/top-seller")
                .param("period", "WEEK")
                .param("date", "2026-05-15"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void getBestPeriod_shouldReturn400_whenWindowDaysNotPositive() throws Exception {
        mockMvc.perform(get("/api/v1/analytics/sellers/1/best-period")
                .param("windowDays", "0"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void getBestPeriod_shouldReturn200_whenFound() throws Exception {
        BestPeriodResponse response = new BestPeriodResponse(
            1L,
            LocalDateTime.of(2026, 5, 1, 0, 0),
            LocalDateTime.of(2026, 5, 8, 0, 0),
            5,
            new BigDecimal("500.00")
        );
        when(analyticsService.findBestPeriod(eq(1L), eq(7)))
            .thenReturn(Optional.of(response));

        mockMvc.perform(get("/api/v1/analytics/sellers/1/best-period")
                .param("windowDays", "7"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.sellerId").value(1))
            .andExpect(jsonPath("$.transactionCount").value(5));
    }
}
