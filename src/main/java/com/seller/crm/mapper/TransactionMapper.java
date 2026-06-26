package com.seller.crm.mapper;

import com.seller.crm.dto.request.TransactionCreateRequest;
import com.seller.crm.dto.response.TransactionResponse;
import com.seller.crm.entity.Transaction;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {
    public Transaction toEntity(TransactionCreateRequest request) {
        Transaction transaction = new Transaction();
        transaction.setAmount(request.amount());
        transaction.setPaymentType(request.paymentType());
        transaction.setTransactionDate(request.transactionDate());
        return transaction;
    }

    public TransactionResponse toResponse(Transaction transaction) {
        return new TransactionResponse(
            transaction.getId(),
            transaction.getSeller().getId(),
            transaction.getAmount(),
            transaction.getPaymentType(),
            transaction.getTransactionDate(),
            transaction.getCreatedAt(),
            transaction.getUpdatedAt()
        );
    }
}
