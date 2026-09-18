package com.puntomartinez.millete.plannedtransactions.infrastructure.out.persistence.postgresql.repository;

import com.puntomartinez.millete.plannedtransactions.infrastructure.out.persistence.postgresql.entity.PlannedTransactionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface JpaPlannedTransactionRepository
        extends JpaRepository<PlannedTransactionEntity, UUID>,
        JpaSpecificationExecutor<PlannedTransactionEntity> {

    List<PlannedTransactionEntity> findByUserIdAndActiveTrueOrderByStartDateDesc(
            UUID userId
    );

    @Query("""
            SELECT pt
            FROM PlannedTransactionEntity pt
            WHERE pt.active = true
              AND (pt.endDate IS NULL OR pt.endDate >= :today)
            """)
    Page<PlannedTransactionEntity> findByActiveTrueAndNotExpired(
            @Param("today") LocalDate today,
            Pageable pageable
    );
}