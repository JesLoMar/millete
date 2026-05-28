package com.puntomartinez.millete.plannedtransactions.infrastructure.out.persistence.postgresql.repository;

import com.puntomartinez.millete.plannedtransactions.infrastructure.out.persistence.postgresql.entity.PlannedTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataPlannedTransactionRepository extends JpaRepository<PlannedTransactionEntity, UUID> {
    List<PlannedTransactionEntity> findAllByUserIdOrderByStartDateDesc(UUID userId);
    List<PlannedTransactionEntity> findByActiveTrue();
}