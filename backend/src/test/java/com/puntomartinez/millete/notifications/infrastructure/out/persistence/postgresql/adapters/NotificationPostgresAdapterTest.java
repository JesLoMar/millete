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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
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

    private static final UUID USER_ID = UUID.randomUUID();

    @Mock
    private JpaNotificationRepository jpaRepository;

    @Mock
    private NotificationEntityMapper mapper;

    @InjectMocks
    private NotificationPostgresAdapter adapter;

    private NotificationEntity notificationEntity() {
        NotificationEntity entity = new NotificationEntity();
        entity.setId(UUID.randomUUID());
        entity.setUserId(USER_ID);
        entity.setType(NotificationType.GOAL_INVITATION);
        entity.setTitle("Goal invitation");
        entity.setMessage("You have been invited");
        entity.setMetadata(Map.of("goalId", UUID.randomUUID().toString()));
        entity.setRead(false);
        entity.setActionRequired(true);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setExpiresAt(LocalDateTime.now().plusDays(7));
        entity.setActive(true);
        return entity;
    }

    private Notification domainNotification() {
        return Notification.create(
                USER_ID,
                NotificationType.GOAL_INVITATION,
                "Goal invitation",
                "You have been invited",
                Map.of("goalId", UUID.randomUUID().toString()),
                true,
                LocalDateTime.now().plusDays(7)
        );
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("Should map domain to entity, save it, and map back to domain")
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
            UUID notificationId = UUID.randomUUID();
            NotificationEntity entity = notificationEntity();
            Notification domain = domainNotification();

            when(jpaRepository.findActiveAndNotExpiredByIdAndUserId(
                    eq(notificationId), eq(USER_ID), any(LocalDateTime.class)
            )).thenReturn(Optional.of(entity));
            when(mapper.toDomain(entity)).thenReturn(domain);

            Optional<Notification> result =
                    adapter.findActiveAndNotExpiredByIdAndUserId(
                            notificationId, USER_ID, LocalDateTime.now()
                    );

            assertThat(result).contains(domain);
        }

        @Test
        @DisplayName("Should return empty when not found")
        void shouldReturnEmptyWhenNotFound() {
            UUID notificationId = UUID.randomUUID();

            when(jpaRepository.findActiveAndNotExpiredByIdAndUserId(
                    eq(notificationId), eq(USER_ID), any(LocalDateTime.class)
            )).thenReturn(Optional.empty());

            Optional<Notification> result =
                    adapter.findActiveAndNotExpiredByIdAndUserId(
                            notificationId, USER_ID, LocalDateTime.now()
                    );

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findActiveAndNotExpiredByUserIdOrderByCreatedAtDesc")
    class FindByUserId {

        @Test
        @DisplayName("Should use PageRequest with sort by createdAt descending")
        void shouldUsePageRequestWithSortByCreatedAtDescending() {
            NotificationEntity entity = notificationEntity();
            Notification domain = domainNotification();

            when(jpaRepository.findActiveAndNotExpiredByUserId(
                    eq(USER_ID), any(LocalDateTime.class), any(Pageable.class)
            )).thenReturn(List.of(entity));
            when(mapper.toDomain(entity)).thenReturn(domain);

            List<Notification> result =
                    adapter.findActiveAndNotExpiredByUserIdOrderByCreatedAtDesc(
                            USER_ID, 20, LocalDateTime.now()
                    );

            ArgumentCaptor<Pageable> pageableCaptor =
                    ArgumentCaptor.forClass(Pageable.class);
            verify(jpaRepository).findActiveAndNotExpiredByUserId(
                    eq(USER_ID), any(LocalDateTime.class), pageableCaptor.capture()
            );

            Pageable capturedPageable = pageableCaptor.getValue();
            assertThat(result).containsExactly(domain);
            assertThat(capturedPageable.getPageNumber()).isZero();
            assertThat(capturedPageable.getPageSize()).isEqualTo(20);
            assertThat(capturedPageable.getSort().getOrderFor("createdAt")).isNotNull();
            assertThat(capturedPageable.getSort().getOrderFor("createdAt").getDirection())
                    .isEqualTo(Sort.Direction.DESC);
        }
    }

    @Nested
    @DisplayName("findActiveAndNotExpiredByUserIdPaginated")
    class FindByUserIdPaginated {

        @Test
        @DisplayName("Should map Page to PaginatedNotifications with correct flags")
        void shouldMapPageToPaginatedNotifications() {
            NotificationEntity entity = notificationEntity();
            Notification domain = domainNotification();

            PageRequest pageRequest = PageRequest.of(
                    0, 25, Sort.by("createdAt").descending()
            );
            Page<NotificationEntity> page =
                    new PageImpl<>(List.of(entity), pageRequest, 1);

            when(jpaRepository.findActiveAndNotExpiredByUserIdPage(
                    eq(USER_ID), any(LocalDateTime.class), any(Pageable.class)
            )).thenReturn(page);
            when(mapper.toDomain(entity)).thenReturn(domain);

            PaginatedNotifications result =
                    adapter.findActiveAndNotExpiredByUserIdPaginated(
                            USER_ID, 0, 25, LocalDateTime.now()
                    );

            assertThat(result.content()).containsExactly(domain);
            assertThat(result.currentPage()).isZero();
            assertThat(result.totalPages()).isEqualTo(1);
            assertThat(result.totalElements()).isEqualTo(1L);
            assertThat(result.size()).isEqualTo(25);
            assertThat(result.first()).isTrue();
            assertThat(result.last()).isTrue();
        }
    }

    @Nested
    @DisplayName("countUnreadActiveAndNotExpiredByUserId")
    class CountUnread {

        @Test
        @DisplayName("Should delegate to repository")
        void shouldDelegateToRepository() {
            when(jpaRepository.countUnreadActiveAndNotExpiredByUserId(
                    eq(USER_ID), any(LocalDateTime.class)
            )).thenReturn(3L);

            long result = adapter.countUnreadActiveAndNotExpiredByUserId(
                    USER_ID, LocalDateTime.now()
            );

            assertThat(result).isEqualTo(3L);
        }
    }

    @Nested
    @DisplayName("findActiveByUserIdAndTypeAndMetadataValue")
    class FindByTypeAndMetadata {

        @Test
        @DisplayName("Should return mapped domain when found")
        void shouldReturnMappedDomainWhenFound() {
            NotificationEntity entity = notificationEntity();
            Notification domain = domainNotification();

            when(jpaRepository.findByUserIdAndActiveTrueAndTypeAndMetadataValue(
                    USER_ID, "GOAL_INVITATION", "goalId", "some-goal-id"
            )).thenReturn(Optional.of(entity));
            when(mapper.toDomain(entity)).thenReturn(domain);

            Optional<Notification> result =
                    adapter.findActiveByUserIdAndTypeAndMetadataValue(
                            USER_ID,
                            NotificationType.GOAL_INVITATION,
                            "goalId",
                            "some-goal-id"
                    );

            assertThat(result).contains(domain);
        }

        @Test
        @DisplayName("Should return empty when not found")
        void shouldReturnEmptyWhenNotFound() {
            when(jpaRepository.findByUserIdAndActiveTrueAndTypeAndMetadataValue(
                    USER_ID, "GOAL_INVITATION", "goalId", "non-existent"
            )).thenReturn(Optional.empty());

            Optional<Notification> result =
                    adapter.findActiveByUserIdAndTypeAndMetadataValue(
                            USER_ID,
                            NotificationType.GOAL_INVITATION,
                            "goalId",
                            "non-existent"
                    );

            assertThat(result).isEmpty();
        }
    }
}