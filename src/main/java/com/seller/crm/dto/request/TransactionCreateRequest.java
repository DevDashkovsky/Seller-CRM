package com.seller.crm.dto.request;

import com.seller.crm.entity.PaymentType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionCreateRequest(

    @NotNull
    Long sellerId,

    @NotNull @DecimalMin("0.01")
    BigDecimal amount,

    @NotNull
    PaymentType paymentType,

    LocalDateTime transactionDate
) {
}
