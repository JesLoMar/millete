package com.puntomartinez.millete.notifications.infrastructure.out.persistence.postgresql.mappers;

import com.puntomartinez.millete.notifications.domain.model.Notification;
import com.puntomartinez.millete.notifications.domain.model.NotificationType;
import com.puntomartinez.millete.notifications.infrastructure.out.persistence.postgresql.entity.NotificationEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("NotificationEntityMapper")
class NotificationEntityMapperTest {

    private final NotificationEntityMapper mapper =
            Mappers.getMapper(NotificationEntityMapper.class);

    @Test
    @DisplayName("Should map domain to entity")
    void shouldMapDomainToEntity() {
        Notification domain = Notification.create(
                UUID.randomUUID(),
                NotificationType.GOAL_INVITATION,
                "New invitation",
                "You have been invited",
                Map.of("goalId", "some-id"),
                true,
                Instant.now().plusDays(7)
        );

        NotificationEntity entity = mapper.toEntity(domain);

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(domain.getId());
        assertThat(entity.getUserId()).isEqualTo(domain.getUserId());
        assertThat(entity.getType()).isEqualTo(NotificationType.GOAL_INVITATION);
        assertThat(entity.getTitle()).isEqualTo("New invitation");
        assertThat(entity.getMessage()).isEqualTo("You have been invited");
        assertThat(entity.isRead()).isFalse();
        assertThat(entity.isActionRequired()).isTrue();
        assertThat(entity.isActive()).isTrue();
    }

    @Test
    @DisplayName("Should map entity to domain")
    void shouldMapEntityToDomain() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime expiresAt = Instant.now().plusDays(7);

        NotificationEntity entity = new NotificationEntity();
        entity.setId(id);
        entity.setUserId(userId);
        entity.setType(NotificationType.SYSTEM);
        entity.setTitle("System alert");
        entity.setMessage("Something happened");
        entity.setMetadata(Map.of("key", "value"));
        entity.setRead(true);
        entity.setActionRequired(false);
        entity.setCreatedAt(createdAt);
        entity.setExpiresAt(expiresAt);
        entity.setActive(true);

        Notification domain = mapper.toDomain(entity);

        assertThat(domain).isNotNull();
        assertThat(domain.getId()).isEqualTo(id);
        assertThat(domain.getUserId()).isEqualTo(userId);
        assertThat(domain.getType()).isEqualTo(NotificationType.SYSTEM);
        assertThat(domain.getTitle()).isEqualTo("System alert");
        assertThat(domain.isRead()).isTrue();
        assertThat(domain.isActionRequired()).isFalse();
        assertThat(domain.isActive()).isTrue();
    }

    @Test
    @DisplayName("Should return null when entity is null")
    void shouldReturnNullWhenEntityIsNull() {
        assertThat(mapper.toDomain(null)).isNull();
    }

    @Test
    @DisplayName("Should preserve data in domain-entity-domain round trip")
    void shouldPreserveDataInRoundTrip() {
        Notification original = Notification.create(
                UUID.randomUUID(),
                NotificationType.GOAL_INVITATION,
                "New invitation",
                "You have been invited to a goal",
                Map.of("goalId", "goal-123"),
                true,
                Instant.now().plusDays(7)
        );

        NotificationEntity entity = mapper.toEntity(original);
        Notification restored = mapper.toDomain(entity);

        assertThat(restored.getId()).isEqualTo(original.getId());
        assertThat(restored.getUserId()).isEqualTo(original.getUserId());
        assertThat(restored.getType()).isEqualTo(original.getType());
        assertThat(restored.getTitle()).isEqualTo(original.getTitle());
        assertThat(restored.getMessage()).isEqualTo(original.getMessage());
        assertThat(restored.isRead()).isEqualTo(original.isRead());
        assertThat(restored.isActionRequired()).isEqualTo(original.isActionRequired());
        assertThat(restored.isActive()).isEqualTo(original.isActive());
    }
}