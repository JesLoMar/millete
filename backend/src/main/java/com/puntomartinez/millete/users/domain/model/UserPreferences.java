package com.puntomartinez.millete.users.domain.model;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UserPreferences {

    private UUID id;
    private UUID userId;
    private Map<String, Object> preferences;
    private Instant createdAt;
    private Instant modifiedAt;

    public UserPreferences() {
        this.preferences = new HashMap<>();
    }

    public UserPreferences(
            UUID id,
            UUID userId,
            Map<String, Object> preferences
    ) {
        this.id = id;
        this.userId = userId;
        this.preferences = preferences != null
                ? new HashMap<>(preferences)
                : new HashMap<>();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public Map<String, Object> getPreferences() {
        return preferences;
    }

    public void setPreferences(Map<String, Object> preferences) {
        this.preferences = preferences != null
                ? new HashMap<>(preferences)
                : new HashMap<>();
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getModifiedAt() {
        return modifiedAt;
    }

    public void setModifiedAt(Instant modifiedAt) {
        this.modifiedAt = modifiedAt;
    }
}
