package com.puntomartinez.millete.categories.infrastructure.out.persistence.postgresql.repository;

import com.puntomartinez.millete.categories.infrastructure.out.persistence.postgresql.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaCategoryRepository extends JpaRepository<CategoryEntity, UUID> {

    List<CategoryEntity> findByUserId(UUID userId);

    @Query("SELECT c FROM CategoryEntity c WHERE c.id = :id AND c.userId = :userId")
    Optional<CategoryEntity> findByIdAndUserId(@Param("id") UUID id, @Param("userId") UUID userId);

    @Query("SELECT c FROM CategoryEntity c WHERE c.userId = :userId AND c.budgetLimit IS NOT NULL")
    List<CategoryEntity> findCategoriesWithBudgetByUserId(@Param("userId") UUID userId);
}