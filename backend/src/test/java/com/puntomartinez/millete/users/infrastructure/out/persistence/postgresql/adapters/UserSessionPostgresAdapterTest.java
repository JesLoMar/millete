package com.puntomartinez.millete.users.infrastructure.out.persistence.postgresql.adapters;

import com.puntomartinez.millete.users.domain.model.UserSession;
import com.puntomartinez.millete.users.infrastructure.out.persistence.postgresql.entity.UserSessionEntity;
import com.puntomartinez.millete.users.infrastructure.out.persistence.postgresql.mappers.UserSessionEntityMapper;
import com.puntomartinez.millete.users.infrastructure.out.persistence.postgresql.repository.JpaUserSessionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserSessionPostgresAdapter")
class UserSessionPostgresAdapterTest {

    @Mock
    private JpaUserSessionRepository repository;

    @Mock
    private UserSessionEntityMapper mapper;

    @InjectMocks
    private UserSessionPostgresAdapter adapter;

    private UserSessionEntity sessionEntity() {
        UserSessionEntity entity = new UserSessionEntity();
        entity.setId(UUID.randomUUID());
        entity.setUserId(UUID.randomUUID());
        entity.setChannel("WEB");
        entity.setActive(true);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setModifiedAt(LocalDateTime.now());
        return entity;
    }

    @Test
    @DisplayName("Should find by userId and channel")
    void shouldFindByUserIdAndChannel() {
        UUID userId = UUID.randomUUID();
        UserSessionEntity entity = sessionEntity();
        UserSession domain = new UserSession();
        domain.setId(entity.getId());

        when(repository.findByUserIdAndChannel(userId, "WEB"))
                .thenReturn(List.of(entity));
        when(mapper.toDomain(entity)).thenReturn(domain);

        List<UserSession> result =
                adapter.findByUserIdAndChannel(userId, "WEB");

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(entity.getId());
    }

    @Test
    @DisplayName("Should find by id and map to domain")
    void shouldFindById() {
        UUID sessionId = UUID.randomUUID();
        UserSessionEntity entity = sessionEntity();
        UserSession domain = new UserSession();
        domain.setId(sessionId);

        when(repository.findById(sessionId))
                .thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(domain);

        Optional<UserSession> result = adapter.findById(sessionId);

        assertThat(result).contains(domain);
    }

    @Test
    @DisplayName("Should save and map back to domain")
    void shouldSave() {
        UserSession domain = new UserSession();
        domain.setId(UUID.randomUUID());

        UserSessionEntity entity = sessionEntity();
        UserSessionEntity savedEntity = sessionEntity();
        UserSession savedDomain = domain;

        when(mapper.toEntity(domain)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(savedEntity);
        when(mapper.toDomain(savedEntity)).thenReturn(savedDomain);

        UserSession result = adapter.save(domain);

        assertThat(result).isSameAs(savedDomain);
    }

    @Test
    @DisplayName("Should delegate existsByIdAndActiveTrue")
    void shouldDelegateExistsByIdAndActiveTrue() {
        UUID sessionId = UUID.randomUUID();
        when(repository.existsByIdAndActiveTrue(sessionId))
                .thenReturn(true);

        boolean result = adapter.existsByIdAndActiveTrue(sessionId);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should find active sessions by userId")
    void shouldFindActiveSessionsByUserId() {
        UUID userId = UUID.randomUUID();
        UserSessionEntity entity = sessionEntity();
        UserSession domain = new UserSession();

        when(repository.findByUserIdAndActiveTrue(userId))
                .thenReturn(List.of(entity));
        when(mapper.toDomain(entity)).thenReturn(domain);

        List<UserSession> result =
                adapter.findByUserIdAndActiveTrue(userId);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Should delegate deactivateAllOtherSessions")
    void shouldDelegateDeactivateAllOtherSessions() {
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();

        adapter.deactivateAllOtherSessions(userId, sessionId);

        verify(repository).deactivateAllOtherSessions(userId, sessionId);
    }

    @Test
    @DisplayName("Should delegate deactivateAllSessions")
    void shouldDelegateDeactivateAllSessions() {
        UUID userId = UUID.randomUUID();

        adapter.deactivateAllSessions(userId);

        verify(repository).deactivateAllSessions(userId);
    }
}