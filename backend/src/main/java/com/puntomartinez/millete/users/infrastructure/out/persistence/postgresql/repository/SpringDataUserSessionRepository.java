package com.puntomartinez.millete.users.infrastructure.out.persistence.postgresql.repository;

import com.puntomartinez.millete.users.infrastructure.out.persistence.postgresql.entity.UserSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataUserSessionRepository extends JpaRepository<UserSessionEntity, UUID> {
    List<UserSessionEntity> findByUserIdAndChannel(UUID userId, String channel);

    boolean existsByIdAndActiveTrue(UUID id);

    List<UserSessionEntity> findByUserIdAndActiveTrue(UUID userId);

    @Modifying
    @Query("UPDATE UserSessionEntity s SET s.active = false WHERE s.userId = :userId AND s.id != :currentSessionId")
    void deactivateAllOtherSessions(UUID userId, UUID currentSessionId);

    @Modifying
    @Query("UPDATE UserSessionEntity s SET s.active = false WHERE s.userId = :userId")
    void deactivateAllSessions(UUID userId);
}
