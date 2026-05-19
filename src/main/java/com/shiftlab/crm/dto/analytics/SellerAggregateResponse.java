package com.shiftlab.crm.dto.analytics;

import java.math.BigDecimal;

public record SellerAggregateResponse(
    Long sellerId,
    String sellerName,
    BigDecimal totalAmount
) {
}
