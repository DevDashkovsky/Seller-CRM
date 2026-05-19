package com.shiftlab.crm.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.shiftlab.crm.dto.request.TransactionCreateRequest;
import com.shiftlab.crm.dto.response.TransactionResponse;
import com.shiftlab.crm.entity.PaymentType;
import com.shiftlab.crm.entity.Seller;
import com.shiftlab.crm.entity.Transaction;
import com.shiftlab.crm.mapper.TransactionMapper;
import com.shiftlab.crm.repository.SellerRepository;
import com.shiftlab.crm.repository.TransactionRepository;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository repository;

    @Mock
    private SellerRepository sellerRepository;

    @Mock
    private TransactionMapper mapper;

    @InjectMocks
    private TransactionService service;

    @Test
    void create_shouldKeepProvidedTransactionDate() {
        LocalDateTime transactionDate = LocalDateTime.of(2026, 5, 17, 12, 0);
        TransactionCreateRequest request = new TransactionCreateRequest(
            1L, new BigDecimal("100.00"), PaymentType.CARD, transactionDate
        );

        Seller seller = new Seller();
        seller.setId(1L);

        Transaction mapped = new Transaction();
        mapped.setAmount(new BigDecimal("100.00"));
        mapped.setPaymentType(PaymentType.CARD);
        mapped.setTransactionDate(transactionDate);

        Transaction saved = new Transaction();
        saved.setId(10L);
        saved.setSeller(seller);
        saved.setAmount(new BigDecimal("100.00"));
        saved.setPaymentType(PaymentType.CARD);
        saved.setTransactionDate(transactionDate);

        TransactionResponse expected = new TransactionResponse(
            10L, 1L, new BigDecimal("100.00"), PaymentType.CARD,
            transactionDate, null, null
        );

        when(sellerRepository.findById(1L)).thenReturn(Optional.of(seller));
        when(mapper.toEntity(request)).thenReturn(mapped);
        when(repository.save(mapped)).thenReturn(saved);
        when(mapper.toResponse(saved)).thenReturn(expected);

        TransactionResponse response = service.create(request);

        assertThat(response).isEqualTo(expected);
        assertThat(mapped.getSeller()).isSameAs(seller);
        assertThat(mapped.getTransactionDate()).isEqualTo(transactionDate);
    }

    @Test
    void create_shouldFillTransactionDate_whenNullProvided() {
        TransactionCreateRequest request = new TransactionCreateRequest(
            1L, new BigDecimal("50.00"), PaymentType.CASH, null
        );

        Seller seller = new Seller();
        seller.setId(1L);

        Transaction mapped = new Transaction();
        mapped.setAmount(new BigDecimal("50.00"));
        mapped.setPaymentType(PaymentType.CASH);
        mapped.setTransactionDate(null);

        LocalDateTime before = LocalDateTime.now().minusNanos(1);

        when(sellerRepository.findById(1L)).thenReturn(Optional.of(seller));
        when(mapper.toEntity(request)).thenReturn(mapped);
        when(repository.save(any(Transaction.class))).thenAnswer(
            invocation -> invocation.getArgument(0)
        );
        when(mapper.toResponse(any(Transaction.class))).thenReturn(
            new TransactionResponse(
                1L, 1L, new BigDecimal("50.00"), PaymentType.CASH,
                null, null, null
            )
        );

        service.create(request);

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(repository).save(captor.capture());
        Transaction passed = captor.getValue();
        assertThat(passed.getSeller()).isSameAs(seller);
        assertThat(passed.getTransactionDate()).isNotNull();
        assertThat(passed.getTransactionDate())
            .isBetween(before, LocalDateTime.now().plusNanos(1));
    }

    @Test
    void create_shouldThrow_whenSellerMissing() {
        TransactionCreateRequest request = new TransactionCreateRequest(
            99L, new BigDecimal("10.00"), PaymentType.TRANSFER, null
        );

        when(sellerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(request))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("99");

        verify(mapper, never()).toEntity(any());
        verify(repository, never()).save(any());
    }

    @Test
    void getById_shouldReturnResponse_whenTransactionExists() {
        Seller seller = new Seller();
        seller.setId(1L);
        Transaction transaction = new Transaction();
        transaction.setId(42L);
        transaction.setSeller(seller);
        transaction.setAmount(new BigDecimal("75.00"));
        transaction.setPaymentType(PaymentType.CARD);

        TransactionResponse expected = new TransactionResponse(
            42L, 1L, new BigDecimal("75.00"), PaymentType.CARD,
            null, null, null
        );

        when(repository.findById(42L)).thenReturn(Optional.of(transaction));
        when(mapper.toResponse(transaction)).thenReturn(expected);

        TransactionResponse response = service.getById(42L);

        assertThat(response).isEqualTo(expected);
    }

    @Test
    void getById_shouldThrow_whenTransactionMissing() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(99L))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("99");

        verify(mapper, never()).toResponse(any());
    }

    @Test
    void list_shouldMapPageContent_andPreserveMetadata() {
        Pageable pageable = PageRequest.of(0, 20);

        Transaction t1 = new Transaction();
        t1.setId(1L);
        Transaction t2 = new Transaction();
        t2.setId(2L);
        Page<Transaction> entityPage = new PageImpl<>(List.of(t1, t2), pageable, 42);

        TransactionResponse r1 = new TransactionResponse(
            1L, 1L, new BigDecimal("10.00"), PaymentType.CARD, null, null, null);
        TransactionResponse r2 = new TransactionResponse(
            2L, 1L, new BigDecimal("20.00"), PaymentType.CARD, null, null, null);

        when(repository.findAll(pageable)).thenReturn(entityPage);
        when(mapper.toResponse(t1)).thenReturn(r1);
        when(mapper.toResponse(t2)).thenReturn(r2);

        Page<TransactionResponse> page = service.list(pageable);

        assertThat(page.getContent()).containsExactly(r1, r2);
        assertThat(page.getTotalElements()).isEqualTo(42);
        assertThat(page.getNumber()).isEqualTo(0);
        assertThat(page.getSize()).isEqualTo(20);
    }

    @Test
    void listBySeller_shouldReturnSellerTransactions() {
        Pageable pageable = PageRequest.of(0, 20);
        Long sellerId = 7L;

        Transaction t1 = new Transaction();
        t1.setId(100L);
        Page<Transaction> entityPage = new PageImpl<>(List.of(t1), pageable, 1);

        TransactionResponse r1 = new TransactionResponse(
            100L, 7L, new BigDecimal("10.00"), PaymentType.CARD, null, null, null);

        when(sellerRepository.existsById(sellerId)).thenReturn(true);
        when(repository.findAllBySellerId(sellerId, pageable)).thenReturn(entityPage);
        when(mapper.toResponse(t1)).thenReturn(r1);

        Page<TransactionResponse> page = service.listBySeller(sellerId, pageable);

        assertThat(page.getContent()).containsExactly(r1);
        assertThat(page.getTotalElements()).isEqualTo(1);
    }

    @Test
    void listBySeller_shouldThrow_whenSellerMissing() {
        Pageable pageable = PageRequest.of(0, 20);
        when(sellerRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.listBySeller(99L, pageable))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("99");

        verify(repository, never()).findAllBySellerId(any(), any());
        verify(mapper, never()).toResponse(any());
    }
}
