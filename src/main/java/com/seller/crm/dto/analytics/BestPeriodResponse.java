package com.seller.crm.dto.analytics;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BestPeriodResponse(
    Long sellerId,
    LocalDateTime from,
    LocalDateTime to,
    int transactionCount,
    BigDecimal totalAmount
) {
}
