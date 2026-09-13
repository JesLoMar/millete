package com.puntomartinez.millete.notifications.infrastructure.out.persistence.postgresql.repository;

import com.puntomartinez.millete.notifications.domain.model.NotificationType;
import com.puntomartinez.millete.notifications.infrastructure.out.persistence.postgresql.entity.NotificationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JpaNotificationRepository
        extends JpaRepository<NotificationEntity, UUID> {

    List<NotificationEntity>
    findByUserIdAndActiveTrueOrderByCreatedAtDesc(
            UUID userId,
            Pageable pageable
    );

    Page<NotificationEntity>
    findAllByUserIdAndActiveTrueOrderByCreatedAtDesc(
            UUID userId,
            Pageable pageable
    );

    long countByUserIdAndActiveTrueAndReadFalse(
            UUID userId
    );

    @Query("""
            SELECT n
            FROM NotificationEntity n
            WHERE n.userId = :userId
              AND n.active = true
              AND n.type = :type
            """)
    List<NotificationEntity> findByUserIdAndActiveTrueAndType(
            @Param("userId") UUID userId,
            @Param("type") NotificationType type
    );

    @Query(
            value = """
                    SELECT *
                    FROM notifications
                    WHERE user_id = :userId
                      AND active = true
                      AND type = :type
                      AND metadata ->> :metadataKey = :metadataValue
                    """,
            nativeQuery = true
    )
    List<NotificationEntity>
    findByUserIdAndActiveTrueAndTypeAndMetadataValue(
            @Param("userId") UUID userId,
            @Param("type") String type,
            @Param("metadataKey") String metadataKey,
            @Param("metadataValue") String metadataValue
    );
}