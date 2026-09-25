package com.puntomartinez.millete.dataexport.infrastructure.out.users;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.puntomartinez.millete.dataexport.domain.model.UserPreferencesSnapshot;
import com.puntomartinez.millete.dataexport.domain.ports.out.UserPreferencesImportPort;
import com.puntomartinez.millete.shared.domain.ports.out.TimeProvider;
import com.puntomartinez.millete.users.domain.model.UserPreferences;
import com.puntomartinez.millete.users.domain.ports.out.UserPreferencesRepository;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
public class UserPreferencesImportAdapter
        implements UserPreferencesImportPort {

    private final UserPreferencesRepository userPreferencesRepository;
    private final ObjectMapper objectMapper;
    private final TimeProvider timeProvider;

    public UserPreferencesImportAdapter(
            UserPreferencesRepository userPreferencesRepository,
            TimeProvider timeProvider
    ) {
        this.userPreferencesRepository = userPreferencesRepository;
        this.objectMapper = new ObjectMapper();
        this.timeProvider = timeProvider;
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

    @Override
    public void save(
            UserPreferencesSnapshot preferences,
            UUID userId
    ) {
        Map<String, Object> preferencesMap =
                fromJson(preferences.preferencesJson());

        UserPreferences existing =
                userPreferencesRepository
                        .findByUserId(userId)
                        .orElse(null);

        if (existing != null) {
            existing.setPreferences(preferencesMap);
            existing.setModifiedAt(timeProvider.now());
            userPreferencesRepository.save(existing);
            return;
        }

        UserPreferences newPreferences =
                new UserPreferences(
                        UUID.randomUUID(),
                        userId,
                        preferencesMap
                );
        newPreferences.setCreatedAt(preferences.createdAt());
        newPreferences.setModifiedAt(preferences.modifiedAt());
        userPreferencesRepository.save(newPreferences);
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

    private Map<String, Object> fromJson(String json) {
        if (json == null || json.isBlank()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(
                    json,
                    new TypeReference<Map<String, Object>>() {}
            );
        } catch (JsonProcessingException e) {
            return new HashMap<>();
        }
    }
}
