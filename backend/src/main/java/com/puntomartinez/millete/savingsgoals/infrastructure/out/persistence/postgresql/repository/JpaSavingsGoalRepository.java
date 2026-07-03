package com.puntomartinez.millete.savingsgoals.infrastructure.out.persistence.postgresql.repository;

import com.puntomartinez.millete.savingsgoals.infrastructure.out.persistence.postgresql.entity.SavingsGoalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaSavingsGoalRepository extends JpaRepository<SavingsGoalEntity, UUID> {

    List<SavingsGoalEntity> findByUserIdAndActiveTrueOrderByCreatedAtDesc(UUID userId);

    @Query("SELECT s FROM SavingsGoalEntity s WHERE s.userId = :userId AND s.status = :status AND s.active = true ORDER BY s.createdAt DESC")
    List<SavingsGoalEntity> findByUserIdAndStatusAndActiveTrue(@Param("userId") UUID userId, @Param("status") String status);

    Optional<SavingsGoalEntity> findByIdAndUserId(UUID id, UUID userId);

    List<SavingsGoalEntity> findAllByUserIdAndActiveTrue(UUID userId);
}
