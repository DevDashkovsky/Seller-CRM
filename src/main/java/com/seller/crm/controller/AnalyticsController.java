package com.seller.crm.controller;

import com.seller.crm.dto.analytics.BestPeriodResponse;
import com.seller.crm.dto.analytics.SellerAggregateResponse;
import com.seller.crm.dto.analytics.TopSellerResponse;
import com.seller.crm.entity.PeriodType;
import com.seller.crm.service.AnalyticsService;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/analytics")
@Validated
public class AnalyticsController {

    private final AnalyticsService service;

    public AnalyticsController(AnalyticsService service) {
        this.service = service;
    }

    @GetMapping("/top-seller")
    public ResponseEntity<TopSellerResponse> getTopSeller(
        @RequestParam PeriodType period,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return service.findTopSeller(period, date)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/sellers/below")
    public ResponseEntity<List<SellerAggregateResponse>> getSellersBelowThreshold(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
        @RequestParam @Positive BigDecimal threshold
    ) {
        return ResponseEntity.ok(service.findSellersBelowThreshold(from, to, threshold));
    }

    @GetMapping("/sellers/{id}/best-period")
    public ResponseEntity<BestPeriodResponse> getBestPeriod(
        @PathVariable Long id,
        @RequestParam @Positive int windowDays
    ) {
        return service.findBestPeriod(id, windowDays)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.noContent().build());
    }
}
