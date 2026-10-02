package com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.repository;

import com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.entity.PositionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface JpaPositionRepository
        extends JpaRepository<PositionEntity, UUID> {

    @Modifying
    @Query("""
        DELETE FROM PositionEntity p
        WHERE p.userId = :userId
    """)
    int deleteAllByUserId(
            @Param("userId") UUID userId
    );
}