package com.shiftlab.crm.entity;

import java.time.LocalDateTime;

public record PeriodRange(
    LocalDateTime from,
    LocalDateTime to
) {
}
