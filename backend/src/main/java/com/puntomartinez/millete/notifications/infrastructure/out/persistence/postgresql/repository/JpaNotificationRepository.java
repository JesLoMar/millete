package com.puntomartinez.millete.notifications.infrastructure.out.persistence.postgresql.repository;

import com.puntomartinez.millete.notifications.infrastructure.out.persistence.postgresql.entity.NotificationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JpaNotificationRepository extends JpaRepository<NotificationEntity, UUID> {

    List<NotificationEntity> findByUserIdAndActiveTrueOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    Page<NotificationEntity> findAllByUserIdAndActiveTrueOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    long countByUserIdAndActiveTrueAndReadFalse(UUID userId);

    List<NotificationEntity> findByUserIdAndActiveTrueAndType(UUID userId, com.puntomartinez.millete.notifications.domain.model.NotificationType type);
}
