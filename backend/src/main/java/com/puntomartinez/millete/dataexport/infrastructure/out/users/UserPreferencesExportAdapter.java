package com.puntomartinez.millete.dataexport.infrastructure.out.users;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final ObjectMapper objectMapper;

    public UserPreferencesExportAdapter(
            UserPreferencesRepository userPreferencesRepository
    ) {
        this.userPreferencesRepository = userPreferencesRepository;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public Optional<UserPreferencesSnapshot> findByUserId(UUID userId) {
        return userPreferencesRepository.findByUserId(userId)
                .map(preferences ->
                        new UserPreferencesSnapshot(
                                preferences.getId(),
                                preferences.getUserId(),
                                toJson(preferences.getPreferences()),
                                preferences.getCreatedAt(),
                                preferences.getModifiedAt()
                        )
                );
    }

    private String toJson(Object value) {
        if (value == null) {
            return "{}";
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }
}