package com.puntomartinez.millete.categories.infrastructure.out.persistence.postgresql.repository;

import com.puntomartinez.millete.categories.infrastructure.out.persistence.postgresql.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaCategoryRepository
        extends JpaRepository<CategoryEntity, UUID>,
        JpaSpecificationExecutor<CategoryEntity> {

    List<CategoryEntity> findByUserId(UUID userId);

    Optional<CategoryEntity> findByIdAndUserId(UUID id, UUID userId);

    List<CategoryEntity> findByUserIdAndIdIn(
            UUID userId,
            List<UUID> categoryIds
    );

    @Query("""
        SELECT c
        FROM CategoryEntity c
        WHERE c.userId = :userId
          AND c.budgetLimit IS NOT NULL
          AND c.active = true
    """)
    List<CategoryEntity> findCategoriesWithBudgetByUserId(
            @Param("userId") UUID userId
    );

    @Query("""
        SELECT COUNT(c)
        FROM CategoryEntity c
        WHERE c.userId = :userId
          AND LOWER(c.name) = LOWER(:name)
          AND c.active = true
    """)
    long countActiveByUserIdAndName(
            @Param("userId") UUID userId,
            @Param("name") String name
    );

    @Query("""
        SELECT COUNT(c)
        FROM CategoryEntity c
        WHERE c.userId = :userId
          AND LOWER(c.name) = LOWER(:name)
          AND c.active = true
          AND c.id != :excludeId
    """)
    long countActiveByUserIdAndNameExcludingId(
            @Param("userId") UUID userId,
            @Param("name") String name,
            @Param("excludeId") UUID excludeId
    );
}