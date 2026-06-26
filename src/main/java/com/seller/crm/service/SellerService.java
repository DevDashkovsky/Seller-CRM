package com.seller.crm.service;


import com.seller.crm.dto.request.SellerCreateRequest;
import com.seller.crm.dto.request.SellerUpdateRequest;
import com.seller.crm.dto.response.SellerResponse;
import com.seller.crm.entity.Seller;
import com.seller.crm.mapper.SellerMapper;
import com.seller.crm.repository.SellerRepository;
import com.seller.crm.repository.TransactionRepository;
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
    private final TransactionRepository transactionRepository;
    private final SellerMapper mapper;

    public SellerService(SellerRepository repository, TransactionRepository transactionRepository, SellerMapper mapper) {
        this.repository = repository;
        this.transactionRepository = transactionRepository;
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

    @Transactional
    public SellerResponse update(Long id, SellerUpdateRequest request) {
        Seller seller = repository.findById(id).orElseThrow(
            () -> new EntityNotFoundException(
                "Seller with id %d not found".formatted(id)
            )
        );
        seller.setName(request.name());
        seller.setContactInfo(request.contactInfo());
        return mapper.toResponse(seller);
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException(
                "Seller with id %d not found".formatted(id)
            );
        }
        transactionRepository.softDeleteBySellerId(id, LocalDateTime.now());
        repository.deleteById(id);
    }
}
