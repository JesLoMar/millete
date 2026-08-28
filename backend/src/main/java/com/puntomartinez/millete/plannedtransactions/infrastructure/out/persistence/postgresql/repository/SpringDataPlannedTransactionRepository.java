package com.puntomartinez.millete.plannedtransactions.infrastructure.out.persistence.postgresql.repository;

import com.puntomartinez.millete.plannedtransactions.infrastructure.out.persistence.postgresql.entity.PlannedTransactionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataPlannedTransactionRepository extends JpaRepository<PlannedTransactionEntity, UUID>, JpaSpecificationExecutor<PlannedTransactionEntity> {
    List<PlannedTransactionEntity> findAllByUserIdOrderByStartDateDesc(UUID userId);
    List<PlannedTransactionEntity> findByActiveTrue();
}
