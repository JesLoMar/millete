package com.puntomartinez.millete.users.infrastructure.out.persistence.postgresql.adapters;

import com.puntomartinez.millete.users.domain.model.UserPreferences;
import com.puntomartinez.millete.users.infrastructure.out.persistence.postgresql.entity.UserPreferencesEntity;
import com.puntomartinez.millete.users.infrastructure.out.persistence.postgresql.mappers.UserPreferencesEntityMapper;
import com.puntomartinez.millete.users.infrastructure.out.persistence.postgresql.repository.JpaUserPreferencesRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserPreferencesPostgresAdapter")
class UserPreferencesPostgresAdapterTest {

    @Mock
    private JpaUserPreferencesRepository repository;

    @Mock
    private UserPreferencesEntityMapper mapper;

    @InjectMocks
    private UserPreferencesPostgresAdapter adapter;

    @Test
    @DisplayName("Should find by userId and map to domain")
    void shouldFindByUserId() {
        UUID userId = UUID.randomUUID();
        UserPreferencesEntity entity = new UserPreferencesEntity();
        entity.setUserId(userId);

        UserPreferences domain = new UserPreferences();
        domain.setUserId(userId);

        when(repository.findByUserId(userId))
                .thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(domain);

        Optional<UserPreferences> result =
                adapter.findByUserId(userId);

        assertThat(result).contains(domain);
    }

    @Test
    @DisplayName("Should save and map back to domain")
    void shouldSave() {
        UserPreferences domain = new UserPreferences();
        domain.setUserId(UUID.randomUUID());

        UserPreferencesEntity entity = new UserPreferencesEntity();
        UserPreferencesEntity savedEntity = new UserPreferencesEntity();
        UserPreferences savedDomain = domain;

        when(mapper.toEntity(domain)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(savedEntity);
        when(mapper.toDomain(savedEntity)).thenReturn(savedDomain);

        UserPreferences result = adapter.save(domain);

        assertThat(result).isSameAs(savedDomain);
    }

    @Test
    @DisplayName("Should delegate deleteByUserId to repository")
    void shouldDelegateDeleteByUserId() {
        UUID userId = UUID.randomUUID();

        adapter.deleteByUserId(userId);

        verify(repository).deleteByUserId(userId);
    }
}