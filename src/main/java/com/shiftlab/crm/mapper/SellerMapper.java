package com.shiftlab.crm.mapper;

import com.shiftlab.crm.dto.request.SellerCreateRequest;
import com.shiftlab.crm.dto.response.SellerResponse;
import com.shiftlab.crm.entity.Seller;
import org.springframework.stereotype.Component;

@Component
public class SellerMapper {

    public Seller toEntity(SellerCreateRequest request) {
        Seller seller = new Seller();
        seller.setName(request.name());
        seller.setContactInfo(request.contactInfo());
        seller.setRegistrationDate(request.registrationDate());
        return seller;
    }

    public SellerResponse toResponse(Seller seller) {
        return new SellerResponse(
            seller.getId(),
            seller.getName(),
            seller.getContactInfo(),
            seller.getRegistrationDate(),
            seller.getCreatedAt(),
            seller.getUpdatedAt()
        );
    }
}
