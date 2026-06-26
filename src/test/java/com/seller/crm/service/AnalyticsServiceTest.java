package com.seller.crm.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.seller.crm.dto.analytics.BestPeriodResponse;
import com.seller.crm.dto.analytics.SellerAggregateResponse;
import com.seller.crm.dto.analytics.TopSellerResponse;
import com.seller.crm.entity.PeriodType;
import com.seller.crm.entity.Transaction;
import com.seller.crm.repository.SellerRepository;
import com.seller.crm.repository.TransactionRepository;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private SellerRepository sellerRepository;

    @InjectMocks
    private AnalyticsService service;

    @Test
    void findTopSeller_shouldReturnResponse_whenRowsFound() {
        LocalDate anchor = LocalDate.of(2026, 5, 15);
        SellerAggregateResponse row = new SellerAggregateResponse(
            7L, "Egor", new BigDecimal("999.99")
        );

        when(transactionRepository.findTopSellersByPeriod(
            eq(LocalDateTime.of(2026, 5, 1, 0, 0)),
            eq(LocalDateTime.of(2026, 6, 1, 0, 0)),
            any(Pageable.class)
        )).thenReturn(List.of(row));

        TopSellerResponse response = service.findTopSeller(PeriodType.MONTH, anchor).orElseThrow();

        assertThat(response.sellerId()).isEqualTo(7L);
        assertThat(response.sellerName()).isEqualTo("Egor");
        assertThat(response.totalAmount()).isEqualTo(new BigDecimal("999.99"));
        assertThat(response.period()).isEqualTo(PeriodType.MONTH);
        assertThat(response.from()).isEqualTo(LocalDateTime.of(2026, 5, 1, 0, 0));
        assertThat(response.to()).isEqualTo(LocalDateTime.of(2026, 6, 1, 0, 0));
    }

    @Test
    void findTopSeller_shouldReturnEmpty_whenNoTransactions() {
        when(transactionRepository.findTopSellersByPeriod(
            any(LocalDateTime.class), any(LocalDateTime.class), any(Pageable.class)
        )).thenReturn(List.of());

        assertThat(service.findTopSeller(PeriodType.YEAR, LocalDate.of(2026, 5, 15)))
            .isEmpty();
    }

    @Test
    void findSellersBelowThreshold_shouldDelegateToRepository() {
        LocalDateTime from = LocalDateTime.of(2026, 5, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2026, 6, 1, 0, 0);
        BigDecimal threshold = new BigDecimal("1000");
        List<SellerAggregateResponse> expected = List.of(
            new SellerAggregateResponse(1L, "A", new BigDecimal("500")),
            new SellerAggregateResponse(2L, "B", new BigDecimal("750"))
        );

        when(transactionRepository.findSellersBelowThreshold(from, to, threshold))
            .thenReturn(expected);

        assertThat(service.findSellersBelowThreshold(from, to, threshold))
            .isEqualTo(expected);
    }

    @Test
    void findSellersBelowThreshold_shouldReturnEmptyList_whenNoMatches() {
        when(transactionRepository.findSellersBelowThreshold(
            any(), any(), any()
        )).thenReturn(List.of());

        assertThat(service.findSellersBelowThreshold(
            LocalDateTime.now(), LocalDateTime.now(), BigDecimal.ZERO
        )).isEmpty();
    }

    @Test
    void findBestPeriod_shouldThrow_whenSellerMissing() {
        when(sellerRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.findBestPeriod(99L, 7))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("99");

        verify(transactionRepository, never())
            .findAllBySellerIdOrderByTransactionDateAsc(any());
    }

    @Test
    void findBestPeriod_shouldReturnEmpty_whenSellerHasNoTransactions() {
        when(sellerRepository.existsById(1L)).thenReturn(true);
        when(transactionRepository.findAllBySellerIdOrderByTransactionDateAsc(1L))
            .thenReturn(List.of());

        assertThat(service.findBestPeriod(1L, 7)).isEmpty();
    }

    @Test
    void findBestPeriod_shouldFindDensestWindow() {
        List<Transaction> txs = List.of(
            tx(LocalDateTime.of(2026, 5, 1, 0, 0), "10"),
            tx(LocalDateTime.of(2026, 5, 2, 0, 0), "20"),
            tx(LocalDateTime.of(2026, 5, 5, 0, 0), "30"),
            tx(LocalDateTime.of(2026, 5, 9, 0, 0), "40"),
            tx(LocalDateTime.of(2026, 5, 10, 0, 0), "50")
        );

        when(sellerRepository.existsById(1L)).thenReturn(true);
        when(transactionRepository.findAllBySellerIdOrderByTransactionDateAsc(1L))
            .thenReturn(txs);

        BestPeriodResponse response = service.findBestPeriod(1L, 3).orElseThrow();

        assertThat(response.sellerId()).isEqualTo(1L);
        assertThat(response.from()).isEqualTo(LocalDateTime.of(2026, 5, 9, 0, 0));
        assertThat(response.to()).isEqualTo(LocalDateTime.of(2026, 5, 12, 0, 0));
        assertThat(response.transactionCount()).isEqualTo(2);
        assertThat(response.totalAmount()).isEqualByComparingTo(new BigDecimal("90"));
    }

    @Test
    void findBestPeriod_shouldWork_whenSingleTransaction() {
        List<Transaction> txs = List.of(tx(LocalDateTime.of(2026, 5, 1, 12, 0), "100"));

        when(sellerRepository.existsById(1L)).thenReturn(true);
        when(transactionRepository.findAllBySellerIdOrderByTransactionDateAsc(1L))
            .thenReturn(txs);

        BestPeriodResponse response = service.findBestPeriod(1L, 7).orElseThrow();

        assertThat(response.transactionCount()).isEqualTo(1);
        assertThat(response.totalAmount()).isEqualByComparingTo(new BigDecimal("100"));
        assertThat(response.from()).isEqualTo(LocalDateTime.of(2026, 5, 1, 12, 0));
        assertThat(response.to()).isEqualTo(LocalDateTime.of(2026, 5, 8, 12, 0));
    }

    private static Transaction tx(LocalDateTime date, String amount) {
        Transaction t = new Transaction();
        t.setTransactionDate(date);
        t.setAmount(new BigDecimal(amount));
        return t;
    }
}
