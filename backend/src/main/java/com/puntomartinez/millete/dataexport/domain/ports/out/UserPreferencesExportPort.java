package com.puntomartinez.millete.dataexport.domain.ports.out;

import com.puntomartinez.millete.dataexport.domain.model.UserPreferencesSnapshot;

import java.util.Optional;
import java.util.UUID;

public interface UserPreferencesExportPort {

    Optional<UserPreferencesSnapshot> findByUserId(UUID userId);
}