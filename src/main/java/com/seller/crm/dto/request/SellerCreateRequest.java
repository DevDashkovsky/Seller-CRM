package com.seller.crm.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record SellerCreateRequest(

    @NotBlank @Size(max = 255)
    String name,

    @Size(max = 255)
    String contactInfo,

    LocalDateTime registrationDate
) {
}
