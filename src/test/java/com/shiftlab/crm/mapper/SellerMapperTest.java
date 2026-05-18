package com.shiftlab.crm.mapper;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.shiftlab.crm.dto.request.SellerCreateRequest;
import com.shiftlab.crm.dto.response.SellerResponse;
import com.shiftlab.crm.entity.Seller;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class SellerMapperTest {

    private final SellerMapper mapper = new SellerMapper();

    @Test
    void toEntity_shouldCopyAllFields() {
        LocalDateTime registrationDate = LocalDateTime.of(2026, 5, 17, 0, 0);
        SellerCreateRequest request = new SellerCreateRequest(
            "Egor",
            "big@Pencil123",
            registrationDate
        );
        Seller entity = mapper.toEntity(request);
        assertThat(entity.getName()).isEqualTo("Egor");
        assertThat(entity.getContactInfo()).isEqualTo("big@Pencil123");
        assertThat(entity.getRegistrationDate()).isEqualTo(registrationDate);
    }

    @Test
    void toEntity_shouldKeepNullRegistrationDate_whenNotProvided() {
        SellerCreateRequest request = new SellerCreateRequest("Egor", null,
            null);

        Seller entity = mapper.toEntity(request);

        assertThat(entity.getRegistrationDate()).isNull();
        assertThat(entity.getContactInfo()).isNull();
    }

    @Test
    void toResponse_shouldCopyAllFields() {
        LocalDateTime now = LocalDateTime.of(2026, 5, 17, 10, 0);
        Seller seller = new Seller();
        seller.setId(42L);
        seller.setName("Egor");
        seller.setContactInfo("big@Pencil123");
        seller.setRegistrationDate(now);
        seller.setCreatedAt(now);
        seller.setUpdatedAt(now);

        SellerResponse response = mapper.toResponse(seller);

        assertThat(response.id()).isEqualTo(42L);
        assertThat(response.name()).isEqualTo("Egor");
        assertThat(response.contactInfo()).isEqualTo("big@Pencil123");
        assertThat(response.registrationDate()).isEqualTo(now);
        assertThat(response.createdAt()).isEqualTo(now);
        assertThat(response.updatedAt()).isEqualTo(now);
    }

}
