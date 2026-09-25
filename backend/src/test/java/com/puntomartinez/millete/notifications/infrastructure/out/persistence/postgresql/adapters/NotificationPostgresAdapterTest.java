package com.puntomartinez.millete.notifications.infrastructure.out.persistence.postgresql.adapters;

import com.puntomartinez.millete.notifications.domain.model.Notification;
import com.puntomartinez.millete.notifications.domain.model.NotificationType;
import com.puntomartinez.millete.notifications.domain.model.PaginatedNotifications;
import com.puntomartinez.millete.notifications.infrastructure.out.persistence.postgresql.entity.NotificationEntity;
import com.puntomartinez.millete.notifications.infrastructure.out.persistence.postgresql.mappers.NotificationEntityMapper;
import com.puntomartinez.millete.notifications.infrastructure.out.persistence.postgresql.repository.JpaNotificationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import com.puntomartinez.millete.shared.domain.time.TimeProvider;
import com.puntomartinez.millete.shared.domain.time.FixedTimeProvider;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationPostgresAdapter")

class NotificationPostgresAdapterTest {
    static final TimeProvider TIME = new FixedTimeProvider(
            Instant.parse("2024-01-15T10:00:00Z"));


    private static final TimeProvider TIME =
            new FixedTimeProvider(Instant.parse("2024-01-01T10:00:00Z"));


    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID NOTIFICATION_ID = UUID.randomUUID();

    @Mock
    private JpaNotificationRepository jpaRepository;

    @Mock
    private NotificationEntityMapper mapper;

    @InjectMocks
    private NotificationPostgresAdapter adapter;

    private NotificationEntity notificationEntity() {
        NotificationEntity entity = new NotificationEntity();
        entity.setId(NOTIFICATION_ID);
        entity.setUserId(USER_ID);
        entity.setType(NotificationType.GOAL_INVITATION);
        entity.setTitle("New invitation");
        entity.setMessage("You have been invited");
        entity.setMetadata(Map.of("goalId", "some-id"));
        entity.setRead(false);
        entity.setActionRequired(true);
        entity.setCreatedAt(Instant.now());
        entity.setExpiresAt(Instant.now().plusDays(7));
        entity.setActive(true);
        return entity;
    }

    private Notification domainNotification() {
        return Notification.create(TIME, 
                USER_ID,
                NotificationType.GOAL_INVITATION,
                "New invitation",
                "You have been invited",
                Map.of("goalId", "some-id"),
                true,
                Instant.now().plusDays(7)
        );
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("Should map domain to entity, save, and map back")
        void shouldSaveNotification() {
            Notification domain = domainNotification();
            NotificationEntity entity = notificationEntity();
            NotificationEntity savedEntity = notificationEntity();
            Notification savedDomain = domainNotification();

            when(mapper.toEntity(domain)).thenReturn(entity);
            when(jpaRepository.save(entity)).thenReturn(savedEntity);
            when(mapper.toDomain(savedEntity)).thenReturn(savedDomain);

            Notification result = adapter.save(domain);

            assertThat(result).isSameAs(savedDomain);
            verify(mapper).toEntity(domain);
            verify(jpaRepository).save(entity);
            verify(mapper).toDomain(savedEntity);
        }
    }

    @Nested
    @DisplayName("findActiveAndNotExpiredByIdAndUserId")
    class FindByIdAndUserId {

        @Test
        @DisplayName("Should return mapped domain when found")
        void shouldReturnMappedDomainWhenFound() {
            NotificationEntity entity = notificationEntity();
            Notification domain = domainNotification();

            when(jpaRepository.findActiveAndNotExpiredByIdAndUserId(
                    eq(NOTIFICATION_ID), eq(USER_ID), any(Instant.class)
            )).thenReturn(Optional.of(entity));
            when(mapper.toDomain(entity)).thenReturn(domain);

            Optional<Notification> result =
                    adapter.findActiveAndNotExpiredByIdAndUserId(
                            NOTIFICATION_ID, USER_ID, Instant.now()
                    );

            assertThat(result).contains(domain);
        }

        @Test
        @DisplayName("Should return empty when not found")
        void shouldReturnEmptyWhenNotFound() {
            when(jpaRepository.findActiveAndNotExpiredByIdAndUserId(
                    eq(NOTIFICATION_ID), eq(USER_ID), any(Instant.class)
            )).thenReturn(Optional.empty());

            Optional<Notification> result =
                    adapter.findActiveAndNotExpiredByIdAndUserId(
                            NOTIFICATION_ID, USER_ID, Instant.now()
                    );

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findActiveAndNotExpiredByUserIdOrderByCreatedAtDesc")
    class FindByUserId {

        @Test
        @DisplayName("Should return mapped notifications")
        void shouldReturnMappedNotifications() {
            NotificationEntity entity = notificationEntity();
            Notification domain = domainNotification();

            when(jpaRepository.findActiveAndNotExpiredByUserId(
                    eq(USER_ID), any(Instant.class), any(Pageable.class)
            )).thenReturn(List.of(entity));
            when(mapper.toDomain(entity)).thenReturn(domain);

            List<Notification> result =
                    adapter.findActiveAndNotExpiredByUserIdOrderByCreatedAtDesc(
                            USER_ID, 25, Instant.now()
                    );

            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("countUnreadActiveAndNotExpiredByUserId")
    class CountUnread {

        @Test
        @DisplayName("Should delegate count to repository")
        void shouldDelegateCount() {
            when(jpaRepository.countUnreadActiveAndNotExpiredByUserId(
                    eq(USER_ID), any(Instant.class)
            )).thenReturn(7L);

            long result = adapter.countUnreadActiveAndNotExpiredByUserId(
                    USER_ID, Instant.now()
            );

            assertThat(result).isEqualTo(7L);
        }
    }

    @Nested
    @DisplayName("findActiveByUserIdAndTypeAndMetadataValue")
    class FindByTypeAndMetadata {

        @Test
        @DisplayName("Should return mapped notification when found")
        void shouldReturnMappedNotification() {
            NotificationEntity entity = notificationEntity();
            Notification domain = domainNotification();

            when(jpaRepository.findByUserIdAndActiveTrueAndTypeAndMetadataValue(
                    USER_ID, "GOAL_INVITATION", "goalId", "some-id"
            )).thenReturn(Optional.of(entity));
            when(mapper.toDomain(entity)).thenReturn(domain);

            Optional<Notification> result =
                    adapter.findActiveByUserIdAndTypeAndMetadataValue(
                            USER_ID,
                            NotificationType.GOAL_INVITATION,
                            "goalId",
                            "some-id"
                    );

            assertThat(result).contains(domain);
        }
    }
}