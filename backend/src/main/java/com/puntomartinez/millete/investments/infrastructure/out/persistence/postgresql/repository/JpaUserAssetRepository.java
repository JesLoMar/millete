package com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.repository;

import com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.entity.UserAssetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaUserAssetRepository
        extends JpaRepository<UserAssetEntity, UUID> {

    Optional<UserAssetEntity> findByIdAndUserId(
            UUID id,
            UUID userId
    );

    List<UserAssetEntity> findByUserIdOrderByNameAscIdAsc(
            UUID userId
    );

    List<UserAssetEntity> findByUserIdAndNameContainingIgnoreCaseOrderByNameAscIdAsc(
            UUID userId,
            String name
    );
}