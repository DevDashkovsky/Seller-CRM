package com.shiftlab.crm.service;


import com.shiftlab.crm.dto.request.TransactionCreateRequest;
import com.shiftlab.crm.dto.response.TransactionResponse;
import com.shiftlab.crm.entity.Seller;
import com.shiftlab.crm.entity.Transaction;
import com.shiftlab.crm.mapper.TransactionMapper;
import com.shiftlab.crm.repository.SellerRepository;
import com.shiftlab.crm.repository.TransactionRepository;
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
}
