package com.seller.crm.repository;

import com.seller.crm.dto.analytics.SellerAggregateResponse;
import com.seller.crm.entity.Transaction;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Page<Transaction> findAllBySellerId(Long sellerId, Pageable pageable);

    List<Transaction> findAllBySellerIdOrderByTransactionDateAsc(Long sellerId);

    @Modifying
    @Query("""
            UPDATE Transaction t
            SET t.deleted = true, t.deletedAt = :now
            WHERE t.seller.id = :sellerId AND t.deleted = false
            """)
    void softDeleteBySellerId(@Param("sellerId") Long sellerId,
                             @Param("now") LocalDateTime now);

    @Query("""
            SELECT new com.seller.crm.dto.analytics.SellerAggregateResponse(
                t.seller.id, t.seller.name, SUM(t.amount))
            FROM Transaction t
            WHERE t.transactionDate >= :from AND t.transactionDate < :to
            GROUP BY t.seller.id, t.seller.name
            ORDER BY SUM(t.amount) DESC, t.seller.id ASC
            """)
    List<SellerAggregateResponse> findTopSellersByPeriod(
            LocalDateTime from,
            LocalDateTime to,
            Pageable pageable
    );

    @Query("""
            SELECT new com.seller.crm.dto.analytics.SellerAggregateResponse(
                t.seller.id, t.seller.name, SUM(t.amount))
            FROM Transaction t
            WHERE t.transactionDate >= :from AND t.transactionDate < :to
            GROUP BY t.seller.id, t.seller.name
            HAVING SUM(t.amount) < :threshold
            """)
    List<SellerAggregateResponse> findSellersBelowThreshold(
            LocalDateTime from,
            LocalDateTime to,
            BigDecimal threshold
    );
}
