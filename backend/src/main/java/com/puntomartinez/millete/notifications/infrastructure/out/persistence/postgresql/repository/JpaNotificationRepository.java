package com.puntomartinez.millete.notifications.infrastructure.out.persistence.postgresql.repository;

import com.puntomartinez.millete.notifications.infrastructure.out.persistence.postgresql.entity.NotificationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaNotificationRepository
        extends JpaRepository<NotificationEntity, UUID> {

    @Query("""
            SELECT n
            FROM NotificationEntity n
            WHERE n.id = :id
              AND n.userId = :userId
              AND n.active = true
              AND (n.expiresAt IS NULL OR n.expiresAt > :now)
            """)
    Optional<NotificationEntity> findActiveAndNotExpiredByIdAndUserId(
            @Param("id") UUID id,
            @Param("userId") UUID userId,
            @Param("now") LocalDateTime now
    );

    @Query("""
            SELECT n
            FROM NotificationEntity n
            WHERE n.userId = :userId
              AND n.active = true
              AND (n.expiresAt IS NULL OR n.expiresAt > :now)
            """)
    List<NotificationEntity> findActiveAndNotExpiredByUserId(
            @Param("userId") UUID userId,
            @Param("now") LocalDateTime now,
            Pageable pageable
    );

    @Query("""
            SELECT n
            FROM NotificationEntity n
            WHERE n.userId = :userId
              AND n.active = true
              AND (n.expiresAt IS NULL OR n.expiresAt > :now)
            """)
    Page<NotificationEntity> findActiveAndNotExpiredByUserIdPage(
            @Param("userId") UUID userId,
            @Param("now") LocalDateTime now,
            Pageable pageable
    );

    @Query("""
            SELECT COUNT(n)
            FROM NotificationEntity n
            WHERE n.userId = :userId
              AND n.active = true
              AND n.read = false
              AND (n.expiresAt IS NULL OR n.expiresAt > :now)
            """)
    long countUnreadActiveAndNotExpiredByUserId(
            @Param("userId") UUID userId,
            @Param("now") LocalDateTime now
    );

    @Query(
            value = """
                    SELECT *
                    FROM notifications
                    WHERE user_id = :userId
                      AND active = true
                      AND type = :type
                      AND metadata ->> :metadataKey = :metadataValue
                    ORDER BY created_at DESC
                    LIMIT 1
                    """,
            nativeQuery = true
    )
    Optional<NotificationEntity> findByUserIdAndActiveTrueAndTypeAndMetadataValue(
            @Param("userId") UUID userId,
            @Param("type") String type,
            @Param("metadataKey") String metadataKey,
            @Param("metadataValue") String metadataValue
    );
}