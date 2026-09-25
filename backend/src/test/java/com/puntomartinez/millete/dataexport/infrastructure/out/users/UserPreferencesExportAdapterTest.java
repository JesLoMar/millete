package com.puntomartinez.millete.dataexport.infrastructure.out.users;

import com.puntomartinez.millete.dataexport.domain.model.UserPreferencesSnapshot;
import com.puntomartinez.millete.users.domain.model.UserPreferences;
import com.puntomartinez.millete.users.domain.ports.out.UserPreferencesRepository;
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
import static org.mockito.Mockito.when;
import java.time.Instant;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserPreferencesExportAdapter")
class UserPreferencesExportAdapterTest {

    @Mock
    private UserPreferencesRepository userPreferencesRepository;

    @InjectMocks
    private UserPreferencesExportAdapter adapter;

    private UUID userId = UUID.randomUUID();

    @Test
    @DisplayName("findByUserId should return preferences snapshot when exists")
    void findByUserIdShouldReturnPreferencesSnapshotWhenExists() {
        UserPreferences prefs = new UserPreferences(
                UUID.randomUUID(), userId, Map.of("theme", "dark")
        );
        prefs.setCreatedAt(Instant.now());
        prefs.setModifiedAt(Instant.now());

        when(userPreferencesRepository.findByUserId(userId))
                .thenReturn(Optional.of(prefs));

        Optional<UserPreferencesSnapshot> result = adapter.findByUserId(userId);

        assertThat(result).isPresent();
        assertThat(result.get().userId()).isEqualTo(userId);
        assertThat(result.get().preferencesJson()).contains("theme");
        assertThat(result.get().preferencesJson()).contains("dark");
    }

    @Test
    @DisplayName("findByUserId should return empty when no preferences")
    void findByUserIdShouldReturnEmptyWhenNoPreferences() {
        when(userPreferencesRepository.findByUserId(userId))
                .thenReturn(Optional.empty());

        Optional<UserPreferencesSnapshot> result = adapter.findByUserId(userId);

        assertThat(result).isEmpty();
    }
}