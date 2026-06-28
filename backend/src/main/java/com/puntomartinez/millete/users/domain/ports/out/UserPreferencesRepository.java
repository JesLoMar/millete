package com.puntomartinez.millete.users.domain.ports.out;

import com.puntomartinez.millete.users.domain.model.UserPreferences;

import java.util.Optional;
import java.util.UUID;

public interface UserPreferencesRepository {
    Optional<UserPreferences> findByUserId(UUID userId);
    UserPreferences save(UserPreferences preferences);
    void deleteByUserId(UUID userId);
}
