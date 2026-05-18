package com.shiftlab.crm.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.shiftlab.crm.dto.request.TransactionCreateRequest;
import com.shiftlab.crm.dto.response.TransactionResponse;
import com.shiftlab.crm.entity.PaymentType;
import com.shiftlab.crm.entity.Seller;
import com.shiftlab.crm.entity.Transaction;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class TransactionMapperTest {

    private final TransactionMapper mapper = new TransactionMapper();

    @Test
    void toEntity_shouldCopyAllFields_exceptSeller() {
        LocalDateTime transactionDate = LocalDateTime.of(2026, 5, 17, 12, 0);
        TransactionCreateRequest request = new TransactionCreateRequest(
            1L,
            new BigDecimal("100.00"),
            PaymentType.CARD,
            transactionDate
        );

        Transaction entity = mapper.toEntity(request);

        assertThat(entity.getAmount()).isEqualTo(new BigDecimal("100.00"));
        assertThat(entity.getPaymentType()).isEqualTo(PaymentType.CARD);
        assertThat(entity.getTransactionDate()).isEqualTo(transactionDate);
        assertThat(entity.getSeller()).isNull();
    }

    @Test
    void toEntity_shouldKeepNullTransactionDate_whenNotProvided() {
        TransactionCreateRequest request = new TransactionCreateRequest(
            1L, new BigDecimal("10.00"), PaymentType.CASH, null
        );

        Transaction entity = mapper.toEntity(request);

        assertThat(entity.getTransactionDate()).isNull();
    }

    @Test
    void toResponse_shouldCopyAllFields_andFlattenSellerId() {
        LocalDateTime now = LocalDateTime.of(2026, 5, 17, 10, 0);
        Seller seller = new Seller();
        seller.setId(7L);

        Transaction transaction = new Transaction();
        transaction.setId(42L);
        transaction.setSeller(seller);
        transaction.setAmount(new BigDecimal("250.50"));
        transaction.setPaymentType(PaymentType.TRANSFER);
        transaction.setTransactionDate(now);
        transaction.setCreatedAt(now);
        transaction.setUpdatedAt(now);

        TransactionResponse response = mapper.toResponse(transaction);

        assertThat(response.id()).isEqualTo(42L);
        assertThat(response.sellerId()).isEqualTo(7L);
        assertThat(response.amount()).isEqualTo(new BigDecimal("250.50"));
        assertThat(response.paymentType()).isEqualTo(PaymentType.TRANSFER);
        assertThat(response.transactionDate()).isEqualTo(now);
        assertThat(response.createdAt()).isEqualTo(now);
        assertThat(response.updatedAt()).isEqualTo(now);
    }
}
