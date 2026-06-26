package com.seller.crm.entity;

import java.time.LocalDateTime;

public record PeriodRange(
    LocalDateTime from,
    LocalDateTime to
) {
}
