package com.puntomartinez.millete.savingsgoals.infrastructure.out.persistence.postgresql.repository;

import com.puntomartinez.millete.savingsgoals.infrastructure.out.persistence.postgresql.entity.SavingsGoalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaSavingsGoalRepository
extends JpaRepository<SavingsGoalEntity, UUID>,
JpaSpecificationExecutor<SavingsGoalEntity> {

Optional<SavingsGoalEntity> findByIdAndUserId(
        UUID id,
        UUID userId
);

List<SavingsGoalEntity> findAllByUserIdAndActiveTrue(
        UUID userId
);
}
