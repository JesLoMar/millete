package com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.repository;

import com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.entity.LotConsumptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface JpaLotConsumptionRepository
        extends JpaRepository<LotConsumptionEntity, UUID> {

    @Modifying
    @Query("""
        DELETE FROM LotConsumptionEntity c
        WHERE c.userId = :userId
    """)
    int deleteAllByUserId(
            @Param("userId") UUID userId
    );
}