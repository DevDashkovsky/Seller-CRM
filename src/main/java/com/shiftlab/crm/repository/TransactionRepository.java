package com.shiftlab.crm.repository;

import com.shiftlab.crm.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Page<Transaction> findAllBySellerId(Long sellerId, Pageable pageable);

    @Query("""
            SELECT t.seller, SUM(t.amount) AS total
            FROM Transaction t
            WHERE t.transactionDate BETWEEN :from AND :to
            GROUP BY t.seller
            ORDER BY total DESC
            """)
    List<Object[]> findTopSellersByPeriod(
            LocalDateTime from,
            LocalDateTime to,
            Pageable pageable
    );

    @Query("""
            SELECT t.seller.id
            FROM Transaction t
            WHERE t.transactionDate BETWEEN :from AND :to
            GROUP BY t.seller.id
            HAVING SUM(t.amount) < :threshold
            """)
    List<Long> findSellerIdsBelowThreshold(
            LocalDateTime from,
            LocalDateTime to,
            BigDecimal threshold
    );
}
