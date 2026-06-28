package com.puntomartinez.millete.users.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class UserPreferences {
    private UUID id;
    private UUID userId;
    private String preferencesJson;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    public UserPreferences() {}

    public UserPreferences(UUID id, UUID userId, String preferencesJson) {
        this.id = id;
        this.userId = userId;
        this.preferencesJson = preferencesJson;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public String getPreferencesJson() { return preferencesJson; }
    public void setPreferencesJson(String preferencesJson) { this.preferencesJson = preferencesJson; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getModifiedAt() { return modifiedAt; }
    public void setModifiedAt(LocalDateTime modifiedAt) { this.modifiedAt = modifiedAt; }
}
