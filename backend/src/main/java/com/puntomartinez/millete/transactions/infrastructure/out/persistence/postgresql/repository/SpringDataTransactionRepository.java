package com.puntomartinez.millete.transactions.infrastructure.out.persistence.postgresql.repository;

import com.puntomartinez.millete.transactions.infrastructure.out.persistence.postgresql.entity.TransactionEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataTransactionRepository
        extends JpaRepository<TransactionEntity, UUID>,
        JpaSpecificationExecutor<TransactionEntity> {

    List<TransactionEntity> findAllByUserIdOrderByDateDesc(UUID userId);

    Optional<TransactionEntity> findByIdAndUserId(
            UUID id,
            UUID userId
    );

    boolean existsByIdAndUserIdAndInvestmentActivityIdIsNotNull(UUID id, UUID userId);

    @Query("""
            SELECT t
            FROM TransactionEntity t
            WHERE t.userId = :userId
              AND t.date >= :start
              AND t.date <= :end
              AND t.active = true
            """)
    List<TransactionEntity> findByUserIdAndDateBetween(
            @Param("userId") UUID userId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );

    List<TransactionEntity> findByUserIdAndActiveTrueOrderByDateDesc(
            UUID userId,
            Pageable pageable
    );

    @Modifying
    @Query("""
            UPDATE TransactionEntity t
            SET t.categoryId = null,
                t.modifiedAt = :modifiedAt
            WHERE t.categoryId = :categoryId
              AND t.userId = :userId
              AND t.active = true
            """)
    void clearCategoryFromActiveTransactions(
            @Param("categoryId") UUID categoryId,
            @Param("userId") UUID userId,
            @Param("modifiedAt") Instant modifiedAt
    );

    @Query("""
        SELECT
            COALESCE(
                SUM(
                    CASE
                        WHEN t.type = 'INCOME' THEN t.amount
                        ELSE 0
                    END
                ),
                0
            ),
            COALESCE(
                SUM(
                    CASE
                        WHEN t.type = 'EXPENSE' THEN t.amount
                        ELSE 0
                    END
                ),
                0
            ),
            COUNT(CASE WHEN t.type IN ('INCOME', 'EXPENSE') THEN 1 ELSE null END),
            COALESCE(SUM(CASE WHEN t.type = 'TRANSFER_IN' THEN t.amount ELSE 0 END), 0),
            COALESCE(SUM(CASE WHEN t.type = 'TRANSFER_OUT' THEN t.amount ELSE 0 END), 0)
        FROM TransactionEntity t
        WHERE t.userId = :userId
          AND t.date >= :start
          AND t.date <= :end
          AND t.active = true
        """)
    List<Object[]> getAggregatesByUserIdAndDateBetween(
            @Param("userId") UUID userId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );
}
