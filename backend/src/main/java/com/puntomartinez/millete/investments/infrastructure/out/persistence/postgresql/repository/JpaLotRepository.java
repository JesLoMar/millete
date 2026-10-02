package com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.repository;

import com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.entity.LotEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface JpaLotRepository
        extends JpaRepository<LotEntity, UUID> {

    @Modifying
    @Query("""
        DELETE FROM LotEntity l
        WHERE l.userId = :userId
    """)
    int deleteAllByUserId(
            @Param("userId") UUID userId
    );
}