package com.seller.crm.service;


import com.seller.crm.dto.request.TransactionCreateRequest;
import com.seller.crm.dto.response.TransactionResponse;
import com.seller.crm.entity.Seller;
import com.seller.crm.entity.Transaction;
import com.seller.crm.mapper.TransactionMapper;
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
public class TransactionService {
    private final TransactionRepository repository;
    private final SellerRepository sellerRepository;
    private final TransactionMapper mapper;

    public TransactionService(
        TransactionRepository repository,
        SellerRepository sellerRepository,
        TransactionMapper mapper
    ) {
        this.repository = repository;
        this.sellerRepository = sellerRepository;
        this.mapper = mapper;
    }

    @Transactional
    public TransactionResponse create(TransactionCreateRequest request) {
        Seller seller = sellerRepository.findById(request.sellerId()).orElseThrow(
            () -> new EntityNotFoundException(
                "Seller with id %d not found".formatted(request.sellerId())
            )
        );
        Transaction transaction = mapper.toEntity(request);
        transaction.setSeller(seller);
        if (transaction.getTransactionDate() == null) {
            transaction.setTransactionDate(LocalDateTime.now());
        }
        Transaction saved = repository.save(transaction);
        return mapper.toResponse(saved);
    }

    public TransactionResponse getById(Long id) {
        Transaction transaction = repository.findById(id).orElseThrow(
            () -> new EntityNotFoundException(
                "Transaction with id %d not found".formatted(id)
            )
        );
        return mapper.toResponse(transaction);
    }

    public Page<TransactionResponse> list(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toResponse);
    }

    public Page<TransactionResponse> listBySeller(Long sellerId, Pageable pageable) {
        if (!sellerRepository.existsById(sellerId)) {
            throw new EntityNotFoundException(
                "Seller with id %d not found".formatted(sellerId)
            );
        }
        return repository.findAllBySellerId(sellerId, pageable).map(mapper::toResponse);
    }
}
