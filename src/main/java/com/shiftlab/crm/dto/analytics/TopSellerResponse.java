package com.shiftlab.crm.dto.analytics;

import com.shiftlab.crm.entity.PeriodType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TopSellerResponse(
    Long sellerId,
    String sellerName,
    BigDecimal totalAmount,
    PeriodType period,
    LocalDateTime from,
    LocalDateTime to
) {
}
