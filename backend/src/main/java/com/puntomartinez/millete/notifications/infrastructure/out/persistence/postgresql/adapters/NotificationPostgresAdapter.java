package com.puntomartinez.millete.notifications.infrastructure.out.persistence.postgresql.adapters;

import com.puntomartinez.millete.notifications.domain.model.Notification;
import com.puntomartinez.millete.notifications.domain.model.NotificationType;
import com.puntomartinez.millete.notifications.domain.model.PaginatedNotifications;
import com.puntomartinez.millete.notifications.domain.ports.out.NotificationRepository;
import com.puntomartinez.millete.notifications.infrastructure.out.persistence.postgresql.entity.NotificationEntity;
import com.puntomartinez.millete.notifications.infrastructure.out.persistence.postgresql.mappers.NotificationEntityMapper;
import com.puntomartinez.millete.notifications.infrastructure.out.persistence.postgresql.repository.JpaNotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class NotificationPostgresAdapter
        implements NotificationRepository {

    private final JpaNotificationRepository jpaRepository;
    private final NotificationEntityMapper mapper;

    @Override
    public Notification save(Notification notification) {
        NotificationEntity entity =
                mapper.toEntity(notification);

        NotificationEntity savedEntity =
                jpaRepository.save(entity);

        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Notification> findActiveAndNotExpiredByIdAndUserId(
            UUID id,
            UUID userId,
            Instant now
    ) {
        return jpaRepository
                .findActiveAndNotExpiredByIdAndUserId(
                        id,
                        userId,
                        now
                )
                .map(mapper::toDomain);
    }

    @Override
    public List<Notification> findActiveAndNotExpiredByUserIdOrderByCreatedAtDesc(
            UUID userId,
            int limit,
            Instant now
    ) {
        return jpaRepository
                .findActiveAndNotExpiredByUserId(
                        userId,
                        now,
                        PageRequest.of(
                                0,
                                limit,
                                Sort.by("createdAt").descending()
                        )
                )
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public PaginatedNotifications findActiveAndNotExpiredByUserIdPaginated(
            UUID userId,
            int page,
            int size,
            Instant now
    ) {
        Page<Notification> pageResult =
                jpaRepository
                        .findActiveAndNotExpiredByUserIdPage(
                                userId,
                                now,
                                PageRequest.of(
                                        page,
                                        size,
                                        Sort.by("createdAt").descending()
                                )
                        )
                        .map(mapper::toDomain);

        return new PaginatedNotifications(
                pageResult.getContent(),
                pageResult.getNumber(),
                pageResult.getTotalPages(),
                pageResult.getTotalElements(),
                pageResult.getSize(),
                pageResult.isFirst(),
                pageResult.isLast()
        );
    }

    @Override
    public long countUnreadActiveAndNotExpiredByUserId(
            UUID userId,
            Instant now
    ) {
        return jpaRepository
                .countUnreadActiveAndNotExpiredByUserId(
                        userId,
                        now
                );
    }

    @Override
    public Optional<Notification> findActiveByUserIdAndTypeAndMetadataValue(
            UUID userId,
            NotificationType type,
            String metadataKey,
            String metadataValue
    ) {
        return jpaRepository
                .findByUserIdAndActiveTrueAndTypeAndMetadataValue(
                        userId,
                        type.name(),
                        metadataKey,
                        metadataValue
                )
                .map(mapper::toDomain);
    }
}