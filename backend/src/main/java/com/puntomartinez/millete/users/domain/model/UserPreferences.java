package com.puntomartinez.millete.users.domain.model;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Preferencias de usuario almacenadas como JSONB.
 *
 * <p>Fase 1 de la normalización temporal: la clave {@link #TIMEZONE_KEY}
 * guarda el identificador de zona IANA del usuario (por ejemplo,
 * {@code Europe/Madrid}). Si la clave está ausente o es inválida, se aplica
 * la zona por defecto técnica {@code UTC} (ver
 * {@link com.puntomartinez.millete.users.domain.validation.ZoneIdValidator}).</p>
 */
public class UserPreferences {

    public static final String TIMEZONE_KEY = "timezone";

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

    /**
     * Devuelve la zona horaria IANA del usuario. Si la clave está ausente,
     * en blanco o no es un string, devuelve el valor por defecto técnico
     * {@code UTC}. No valida identificadores de offset: para validación
     * estricta usar {@link com.puntomartinez.millete.users.domain.validation.ZoneIdValidator}.
     */
    public String getTimezoneOrDefault() {
        Object value = preferences.get(TIMEZONE_KEY);
        if (value instanceof String s && !s.isBlank()) {
            return s.trim();
        }
        return com.puntomartinez.millete.users.domain.validation.ZoneIdValidator.DEFAULT_TIMEZONE;
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