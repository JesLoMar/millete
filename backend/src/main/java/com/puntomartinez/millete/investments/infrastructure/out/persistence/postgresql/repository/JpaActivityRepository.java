package com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.repository;

import com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.entity.ActivityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaActivityRepository
        extends JpaRepository<ActivityEntity, UUID>,
        JpaSpecificationExecutor<ActivityEntity> {

    Optional<ActivityEntity> findByIdAndUserId(
            UUID id,
            UUID userId
    );

    List<ActivityEntity> findByUserIdOrderByOccurredAtAscOrderingKeyAscIdAsc(
            UUID userId
    );
}