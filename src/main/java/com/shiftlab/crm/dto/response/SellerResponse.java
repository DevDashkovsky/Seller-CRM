package com.shiftlab.crm.dto.response;

import java.time.LocalDateTime;

public record SellerResponse(
    Long id,
    String name,
    String contactInfo,
    LocalDateTime registrationDate,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
