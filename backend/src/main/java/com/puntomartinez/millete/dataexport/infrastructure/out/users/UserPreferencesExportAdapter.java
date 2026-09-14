package com.puntomartinez.millete.dataexport.infrastructure.out.users;

import com.puntomartinez.millete.dataexport.domain.model.UserPreferencesSnapshot;
import com.puntomartinez.millete.dataexport.domain.ports.out.UserPreferencesExportPort;
import com.puntomartinez.millete.users.domain.ports.out.UserPreferencesRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class UserPreferencesExportAdapter
        implements UserPreferencesExportPort {

    private final UserPreferencesRepository userPreferencesRepository;

    public UserPreferencesExportAdapter(
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
}