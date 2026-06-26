package com.seller.crm.dto.response;

import com.seller.crm.entity.PaymentType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse (
    Long id,
    Long sellerId,
    BigDecimal amount,
    PaymentType paymentType,
    LocalDateTime transactionDate,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
