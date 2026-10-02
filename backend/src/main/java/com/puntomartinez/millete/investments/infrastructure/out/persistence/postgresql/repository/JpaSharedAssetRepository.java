package com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.repository;

import com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.entity.SharedAssetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaSharedAssetRepository
        extends JpaRepository<SharedAssetEntity, UUID> {

    List<SharedAssetEntity> findAllByOrderByNameAscIdAsc();

    Optional<SharedAssetEntity> findByStableCatalogId(
            String stableCatalogId
    );

    @Query("""
        SELECT a
        FROM SharedAssetEntity a
        WHERE LOWER(a.name) LIKE LOWER(CONCAT('%', :search, '%'))
           OR LOWER(COALESCE(a.symbol, '')) LIKE LOWER(CONCAT('%', :search, '%'))
           OR LOWER(a.stableCatalogId) LIKE LOWER(CONCAT('%', :search, '%'))
        ORDER BY a.name ASC, a.id ASC
    """)
    List<SharedAssetEntity> search(
            @Param("search") String search
    );
}