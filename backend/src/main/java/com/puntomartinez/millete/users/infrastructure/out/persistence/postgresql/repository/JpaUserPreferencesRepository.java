package com.puntomartinez.millete.users.infrastructure.out.persistence.postgresql.repository;

import com.puntomartinez.millete.users.infrastructure.out.persistence.postgresql.entity.UserPreferencesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaUserPreferencesRepository extends JpaRepository<UserPreferencesEntity, UUID> {
    Optional<UserPreferencesEntity> findByUserId(UUID userId);
    void deleteByUserId(UUID userId);
}
