package com.puntomartinez.millete.notifications.infrastructure.out.persistence.postgresql.adapters;

import com.puntomartinez.millete.notifications.domain.model.Notification;
import com.puntomartinez.millete.notifications.domain.model.NotificationType;
import com.puntomartinez.millete.notifications.domain.ports.out.NotificationRepository;
import com.puntomartinez.millete.notifications.infrastructure.out.persistence.postgresql.entity.NotificationEntity;
import com.puntomartinez.millete.notifications.infrastructure.out.persistence.postgresql.mappers.NotificationEntityMapper;
import com.puntomartinez.millete.notifications.infrastructure.out.persistence.postgresql.repository.JpaNotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class NotificationPostgresAdapter implements NotificationRepository {

    private final JpaNotificationRepository jpaRepository;
    private final NotificationEntityMapper mapper;

    @Override
    public Notification save(Notification notification) {
        NotificationEntity entity = mapper.toEntity(notification);
        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<Notification> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Notification> findActiveByUserIdOrderByCreatedAtDesc(UUID userId, int limit) {
        return jpaRepository.findByUserIdAndActiveTrueOrderByCreatedAtDesc(userId, PageRequest.of(0, limit)).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Page<Notification> findActiveByUserIdPaginated(UUID userId, int page, int size) {
        return jpaRepository.findAllByUserIdAndActiveTrueOrderByCreatedAtDesc(userId, PageRequest.of(page, size))
                .map(mapper::toDomain);
    }

    @Override
    public long countUnreadByUserId(UUID userId) {
        return jpaRepository.countByUserIdAndActiveTrueAndReadFalse(userId);
    }

    @Override
    public List<Notification> findActiveByUserIdAndTypeAndMetadataValue(UUID userId, String type, String metadataKey, String metadataValue) {
        return jpaRepository.findByUserIdAndActiveTrueAndType(userId, NotificationType.valueOf(type))
                .stream()
                .filter(n -> {
                    Object value = n.getMetadata().get(metadataKey);
                    return value != null && value.toString().equals(metadataValue);
                })
                .map(mapper::toDomain)
                .toList();
    }
}
