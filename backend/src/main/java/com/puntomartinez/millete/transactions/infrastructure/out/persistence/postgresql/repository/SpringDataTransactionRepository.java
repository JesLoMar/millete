package com.puntomartinez.millete.transactions.infrastructure.out.persistence.postgresql.repository;

import com.puntomartinez.millete.transactions.infrastructure.out.persistence.postgresql.entity.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataTransactionRepository extends JpaRepository<TransactionEntity, UUID> {

    List<TransactionEntity> findAllByUserIdOrderByDateDesc(UUID userId);

    @Query("SELECT t FROM TransactionEntity t WHERE t.userId = :userId AND t.date >= :start AND t.date <= :end AND t.active = true")
    List<TransactionEntity> findByUserIdAndDateBetween(
            @Param("userId") UUID userId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    List<TransactionEntity> findTop5ByUserIdOrderByDateDesc(UUID userId);

    List<TransactionEntity> findAllByCategoryId(UUID categoryId);
}