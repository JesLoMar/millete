package com.puntomartinez.millete.dataexport.domain.ports.out;

import com.puntomartinez.millete.dataexport.domain.model.UserPreferencesSnapshot;

import java.util.Optional;
import java.util.UUID;

public interface UserPreferencesImportPort {

    Optional<UserPreferencesSnapshot> findByUserId(UUID userId);

    void save(
            UserPreferencesSnapshot preferences,
            UUID userId
    );
}