package com.shiftlab.crm.service;


import com.shiftlab.crm.dto.analytics.BestPeriodResponse;
import com.shiftlab.crm.dto.analytics.SellerAggregateResponse;
import com.shiftlab.crm.dto.analytics.TopSellerResponse;
import com.shiftlab.crm.entity.PeriodRange;
import com.shiftlab.crm.entity.PeriodType;
import com.shiftlab.crm.entity.Transaction;
import com.shiftlab.crm.repository.SellerRepository;
import com.shiftlab.crm.repository.TransactionRepository;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AnalyticsService {

    private final TransactionRepository transactionRepository;
    private final SellerRepository sellerRepository;

    public AnalyticsService(TransactionRepository transactionRepository, SellerRepository sellerRepository) {
        this.transactionRepository = transactionRepository;
        this.sellerRepository = sellerRepository;
    }

    public Optional<TopSellerResponse> findTopSeller(PeriodType period, LocalDate anchor) {
        PeriodRange range = period.toRange(anchor.atStartOfDay());
        List<SellerAggregateResponse> rows = transactionRepository.findTopSellersByPeriod(range.from(), range.to(),
            PageRequest.of(0, 1));
        if (rows.isEmpty()) {
            return Optional.empty();
        }
        SellerAggregateResponse top = rows.get(0);
        return Optional.of(new TopSellerResponse(top.sellerId(), top.sellerName(), top.totalAmount(), period, range.from(), range.to()));
    }

    public List<SellerAggregateResponse> findSellersBelowThreshold(LocalDateTime from, LocalDateTime to, BigDecimal threshold) {
        return transactionRepository.findSellersBelowThreshold(from, to, threshold);
    }

    public Optional<BestPeriodResponse> findBestPeriod(Long sellerId, int windowDays) {
        if (!sellerRepository.existsById(sellerId)) {
            throw new EntityNotFoundException(
                "Seller with id %d not found".formatted(sellerId)
            );
        }
        List<Transaction> txs = transactionRepository.findAllBySellerIdOrderByTransactionDateAsc(sellerId);
        if (txs.isEmpty()) {
            return Optional.empty();
        }

        int bestCount = 0;
        BigDecimal bestSum = BigDecimal.ZERO;
        int bestI = 0;

        int j = 0;
        BigDecimal currentSum = BigDecimal.ZERO;

        for (int i = 0; i < txs.size(); i++) {
            LocalDateTime windowEnd = txs.get(i).getTransactionDate().plusDays(windowDays);
            while (j < txs.size() && txs.get(j).getTransactionDate().isBefore(windowEnd)) {
                currentSum = currentSum.add(txs.get(j).getAmount());
                j++;
            }
            int count = j - i;
            if (count > bestCount || (count == bestCount && currentSum.compareTo(bestSum) > 0)) {
                bestCount = count;
                bestSum = currentSum;
                bestI = i;
            }
            currentSum = currentSum.subtract(txs.get(i).getAmount());
        }

        LocalDateTime from = txs.get(bestI).getTransactionDate();
        LocalDateTime to = from.plusDays(windowDays);
        return Optional.of(new BestPeriodResponse(sellerId, from, to, bestCount, bestSum));
    }
}
