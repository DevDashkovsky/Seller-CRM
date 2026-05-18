package com.shiftlab.crm.service;


import com.shiftlab.crm.dto.request.SellerCreateRequest;
import com.shiftlab.crm.dto.response.SellerResponse;
import com.shiftlab.crm.entity.Seller;
import com.shiftlab.crm.mapper.SellerMapper;
import com.shiftlab.crm.repository.SellerRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class SellerService {
    private final SellerRepository repository;
    private final SellerMapper mapper;

    public SellerService(SellerRepository repository, SellerMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional
    public SellerResponse create(SellerCreateRequest request) {
        Seller seller = mapper.toEntity(request);
        if (seller.getRegistrationDate() == null) {
            seller.setRegistrationDate(LocalDateTime.now());
        }
        Seller saved = repository.save(seller);
        return mapper.toResponse(saved);
    }

    public SellerResponse getById(Long id) {
        Seller seller = repository.findById(id).orElseThrow(
            () -> new EntityNotFoundException(
                "Seller with id %d not found".formatted(id)
            )
        );
        return mapper.toResponse(seller);
    }

    public Page<SellerResponse> list(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toResponse);
    }
}
