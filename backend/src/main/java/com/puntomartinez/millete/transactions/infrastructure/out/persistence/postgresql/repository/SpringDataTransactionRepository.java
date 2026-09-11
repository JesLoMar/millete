package com.puntomartinez.millete.transactions.infrastructure.out.persistence.postgresql.repository;

import com.puntomartinez.millete.transactions.infrastructure.out.persistence.postgresql.entity.TransactionEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataTransactionRepository
        extends JpaRepository<TransactionEntity, UUID>,
                JpaSpecificationExecutor<TransactionEntity> {

    List<TransactionEntity> findAllByUserIdOrderByDateDesc(UUID userId);

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
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
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
    int clearCategoryFromActiveTransactions(
            @Param("categoryId") UUID categoryId,
            @Param("userId") UUID userId,
            @Param("modifiedAt") LocalDateTime modifiedAt
    );
}