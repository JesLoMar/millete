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
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime expiresAt = LocalDateTime.of(2024, 12, 31, 23, 59);
        LocalDateTime actionedAt = LocalDateTime.of(2024, 1, 2, 10, 0);
        Map<String, Object> metadata = Map.of("goalId", UUID.randomUUID().toString());

        Notification domain = Notification.reconstitute(
                id,
                userId,
                NotificationType.GOAL_INVITATION,
                "Goal invitation",
                "You have been invited",
                metadata,
                false,
                true,
                actionedAt,
                createdAt,
                expiresAt,
                true
        );

        NotificationEntity entity = mapper.toEntity(domain);

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getUserId()).isEqualTo(userId);
        assertThat(entity.getType()).isEqualTo(NotificationType.GOAL_INVITATION);
        assertThat(entity.getTitle()).isEqualTo("Goal invitation");
        assertThat(entity.getMessage()).isEqualTo("You have been invited");
        assertThat(entity.getMetadata()).containsKey("goalId");
        assertThat(entity.isRead()).isFalse();
        assertThat(entity.isActionRequired()).isTrue();
        assertThat(entity.getActionedAt()).isEqualTo(actionedAt);
        assertThat(entity.getCreatedAt()).isEqualTo(createdAt);
        assertThat(entity.getExpiresAt()).isEqualTo(expiresAt);
        assertThat(entity.isActive()).isTrue();
    }

    @Test
    @DisplayName("Should map entity to domain")
    void shouldMapEntityToDomain() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime expiresAt = LocalDateTime.of(2024, 12, 31, 23, 59);
        LocalDateTime actionedAt = LocalDateTime.of(2024, 1, 2, 10, 0);
        Map<String, Object> metadata = Map.of("goalId", UUID.randomUUID().toString());

        NotificationEntity entity = new NotificationEntity();
        entity.setId(id);
        entity.setUserId(userId);
        entity.setType(NotificationType.GOAL_INVITATION);
        entity.setTitle("Goal invitation");
        entity.setMessage("You have been invited");
        entity.setMetadata(metadata);
        entity.setRead(false);
        entity.setActionRequired(true);
        entity.setActionedAt(actionedAt);
        entity.setCreatedAt(createdAt);
        entity.setExpiresAt(expiresAt);
        entity.setActive(true);

        Notification domain = mapper.toDomain(entity);

        assertThat(domain).isNotNull();
        assertThat(domain.getId()).isEqualTo(id);
        assertThat(domain.getUserId()).isEqualTo(userId);
        assertThat(domain.getType()).isEqualTo(NotificationType.GOAL_INVITATION);
        assertThat(domain.getTitle()).isEqualTo("Goal invitation");
        assertThat(domain.getMessage()).isEqualTo("You have been invited");
        assertThat(domain.getMetadata()).containsKey("goalId");
        assertThat(domain.isRead()).isFalse();
        assertThat(domain.isActionRequired()).isTrue();
        assertThat(domain.getActionedAt()).isEqualTo(actionedAt);
        assertThat(domain.getCreatedAt()).isEqualTo(createdAt);
        assertThat(domain.getExpiresAt()).isEqualTo(expiresAt);
        assertThat(domain.isActive()).isTrue();
    }

    @Test
    @DisplayName("Should return null when entity is null")
    void shouldReturnNullWhenEntityIsNull() {
        assertThat(mapper.toDomain(null)).isNull();
    }

    @Test
    @DisplayName("Should return null when domain is null")
    void shouldReturnNullWhenDomainIsNull() {
        assertThat(mapper.toEntity(null)).isNull();
    }

    @Test
    @DisplayName("Should preserve data in domain-entity-domain round trip")
    void shouldPreserveDataInRoundTrip() {
        Notification original = Notification.create(
                UUID.randomUUID(),
                NotificationType.SYSTEM,
                "System alert",
                "Something happened",
                Map.of("key", "value"),
                true,
                LocalDateTime.now().plusDays(7)
        );

        NotificationEntity entity = mapper.toEntity(original);
        Notification restored = mapper.toDomain(entity);

        assertThat(restored.getId()).isEqualTo(original.getId());
        assertThat(restored.getUserId()).isEqualTo(original.getUserId());
        assertThat(restored.getType()).isEqualTo(original.getType());
        assertThat(restored.getTitle()).isEqualTo(original.getTitle());
        assertThat(restored.getMessage()).isEqualTo(original.getMessage());
        assertThat(restored.getMetadata()).isEqualTo(original.getMetadata());
        assertThat(restored.isRead()).isEqualTo(original.isRead());
        assertThat(restored.isActionRequired()).isEqualTo(original.isActionRequired());
        assertThat(restored.getActionedAt()).isEqualTo(original.getActionedAt());
        assertThat(restored.getCreatedAt()).isEqualTo(original.getCreatedAt());
        assertThat(restored.getExpiresAt()).isEqualTo(original.getExpiresAt());
        assertThat(restored.isActive()).isEqualTo(original.isActive());
    }
}