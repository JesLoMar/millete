package com.puntomartinez.millete.dataexport.infrastructure.out.users;

import com.puntomartinez.millete.dataexport.domain.model.UserPreferencesSnapshot;
import com.puntomartinez.millete.users.domain.model.UserPreferences;
import com.puntomartinez.millete.users.domain.ports.out.UserPreferencesRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserPreferencesImportAdapter")
class UserPreferencesImportAdapterTest {

    @Mock
    private UserPreferencesRepository userPreferencesRepository;

    @InjectMocks
    private UserPreferencesImportAdapter adapter;

    private UUID userId = UUID.randomUUID();

    @Test
    @DisplayName("save should create new preferences when not existing")
    void saveShouldCreateNewPreferencesWhenNotExisting() {
        UserPreferencesSnapshot snapshot = new UserPreferencesSnapshot(
                UUID.randomUUID(), UUID.randomUUID(),
                "{\"theme\":\"dark\"}",
                LocalDateTime.now(), LocalDateTime.now()
        );

        when(userPreferencesRepository.findByUserId(userId))
                .thenReturn(Optional.empty());
        when(userPreferencesRepository.save(any(UserPreferences.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        adapter.save(snapshot, userId);

        ArgumentCaptor<UserPreferences> captor =
                ArgumentCaptor.forClass(UserPreferences.class);
        verify(userPreferencesRepository).save(captor.capture());

        UserPreferences savedPrefs = captor.getValue();
        assertThat(savedPrefs.getUserId()).isEqualTo(userId);
        assertThat(savedPrefs.getPreferences()).containsKey("theme");
        assertThat(savedPrefs.getPreferences().get("theme")).isEqualTo("dark");
    }

    @Test
    @DisplayName("save should update existing preferences")
    void saveShouldUpdateExistingPreferences() {
        UUID existingPrefsId = UUID.randomUUID();
        UserPreferences existingPrefs = new UserPreferences(
                existingPrefsId, userId, java.util.Map.of("theme", "light")
        );

        UserPreferencesSnapshot snapshot = new UserPreferencesSnapshot(
                UUID.randomUUID(), UUID.randomUUID(),
                "{\"theme\":\"dark\"}",
                LocalDateTime.now(), LocalDateTime.now()
        );

        when(userPreferencesRepository.findByUserId(userId))
                .thenReturn(Optional.of(existingPrefs));
        when(userPreferencesRepository.save(any(UserPreferences.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        adapter.save(snapshot, userId);

        ArgumentCaptor<UserPreferences> captor =
                ArgumentCaptor.forClass(UserPreferences.class);
        verify(userPreferencesRepository).save(captor.capture());

        UserPreferences savedPrefs = captor.getValue();
        assertThat(savedPrefs.getId()).isEqualTo(existingPrefsId);
        assertThat(savedPrefs.getUserId()).isEqualTo(userId);
        assertThat(savedPrefs.getPreferences().get("theme")).isEqualTo("dark");
    }
}