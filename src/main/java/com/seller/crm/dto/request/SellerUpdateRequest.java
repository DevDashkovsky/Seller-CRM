package com.seller.crm.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SellerUpdateRequest(

    @NotBlank @Size(max = 255)
    String name,

    @Size(max = 255)
    String contactInfo
) {
}
