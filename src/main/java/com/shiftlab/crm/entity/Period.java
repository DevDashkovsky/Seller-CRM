package com.shiftlab.crm.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

public enum Period {
    DAY,
    MONTH,
    QUARTER,
    YEAR;

    public PeriodRange toRange(LocalDateTime anchor) {
        LocalDate fromDate = startOfPeriod(anchor.toLocalDate());
        LocalDate toDate = switch (this) {
            case DAY -> fromDate.plusDays(1);
            case MONTH -> fromDate.plusMonths(1);
            case QUARTER -> fromDate.plusMonths(3);
            case YEAR -> fromDate.plusYears(1);
        };
        return new PeriodRange(fromDate.atStartOfDay(), toDate.atStartOfDay());
    }

    private LocalDate startOfPeriod(LocalDate date) {
        return switch (this) {
            case DAY -> date;
            case MONTH -> date.withDayOfMonth(1);
            case QUARTER -> {
                int firstMonth = ((date.getMonthValue() - 1) / 3) * 3 + 1;
                yield date.withMonth(firstMonth).withDayOfMonth(1);
            }
            case YEAR -> date.withDayOfYear(1);
        };
    }
}
