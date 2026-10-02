package com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.repository;

import com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.entity.HoldingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JpaHoldingRepository
        extends JpaRepository<HoldingEntity, UUID> {

    List<HoldingEntity>
    findByUserIdOrderBySnapshotAtAscCreatedAtAscIdAsc(
            UUID userId
    );
}