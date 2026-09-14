package com.puntomartinez.millete.dataexport.infrastructure.out.users;

import com.puntomartinez.millete.dataexport.domain.model.UserPreferencesSnapshot;
import com.puntomartinez.millete.dataexport.domain.ports.out.UserPreferencesImportPort;
import com.puntomartinez.millete.users.domain.model.UserPreferences;
import com.puntomartinez.millete.users.domain.ports.out.UserPreferencesRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Component
public class UserPreferencesImportAdapter
        implements UserPreferencesImportPort {

    private final UserPreferencesRepository userPreferencesRepository;

    public UserPreferencesImportAdapter(
            UserPreferencesRepository userPreferencesRepository
    ) {
        this.userPreferencesRepository = userPreferencesRepository;
    }

    @Override
    public Optional<UserPreferencesSnapshot> findByUserId(UUID userId) {
        return userPreferencesRepository.findByUserId(userId)
                .map(preferences ->
                        new UserPreferencesSnapshot(
                                preferences.getId(),
                                preferences.getUserId(),
                                preferences.getPreferencesJson(),
                                preferences.getCreatedAt(),
                                preferences.getModifiedAt()
                        )
                );
    }

    @Override
    public void save(
            UserPreferencesSnapshot preferences,
            UUID userId
    ) {
        UserPreferences existing =
                userPreferencesRepository
                        .findByUserId(userId)
                        .orElse(null);

        if (existing != null) {
            existing.setPreferencesJson(
                    preferences.preferencesJson()
            );
            existing.setModifiedAt(
                    LocalDateTime.now()
            );

            userPreferencesRepository.save(existing);
            return;
        }

        UserPreferences newPreferences =
                new UserPreferences(
                        UUID.randomUUID(),
                        userId,
                        preferences.preferencesJson()
                );

        newPreferences.setCreatedAt(
                preferences.createdAt()
        );

        newPreferences.setModifiedAt(
                preferences.modifiedAt()
        );

        userPreferencesRepository.save(newPreferences);
    }
}