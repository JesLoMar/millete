package com.puntomartinez.millete.notifications.domain.model;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public class Notification {

    private final UUID id;
    private final UUID userId;
    private final NotificationType type;
    private final String title;
    private final String message;
    private final Map<String, Object> metadata;
    private boolean read;
    private final boolean actionRequired;
    private LocalDateTime actionedAt;
    private final LocalDateTime createdAt;
    private final LocalDateTime expiresAt;
    private boolean active;

    private Notification(
        UUID id,
        UUID userId,
        NotificationType type,
        String title,
        String message,
        Map<String, Object> metadata,
        boolean read,
        boolean actionRequired,
        LocalDateTime actionedAt,
        LocalDateTime createdAt,
        LocalDateTime expiresAt,
        boolean active) {

    validateRequiredFields(id, userId, type, title, createdAt);

    this.id = id;
    this.userId = userId;
    this.type = type;
    this.title = title;
    this.message = message;
    this.metadata = metadata == null
            ? null
            : Map.copyOf(metadata);
    this.read = read;
    this.actionRequired = actionRequired;
    this.actionedAt = actionedAt;
    this.createdAt = createdAt;
    this.expiresAt = expiresAt;
    this.active = active;
}

    public static Notification create(
            UUID userId,
            NotificationType type,
            String title,
            String message,
            Map<String, Object> metadata,
            boolean actionRequired,
            LocalDateTime expiresAt) {

        return new Notification(
                UUID.randomUUID(),
                userId,
                type,
                title,
                message,
                metadata,
                false,
                actionRequired,
                null,
                LocalDateTime.now(),
                expiresAt,
                true
        );
    }

    public static Notification reconstitute(
            UUID id,
            UUID userId,
            NotificationType type,
            String title,
            String message,
            Map<String, Object> metadata,
            boolean read,
            boolean actionRequired,
            LocalDateTime actionedAt,
            LocalDateTime createdAt,
            LocalDateTime expiresAt,
            boolean active) {

        return new Notification(
                id,
                userId,
                type,
                title,
                message,
                metadata,
                read,
                actionRequired,
                actionedAt,
                createdAt,
                expiresAt,
                active
        );
    }

    public void markAsRead() {
        this.read = true;
    }

    public void markAsActioned() {
        if (!actionRequired) {
            throw new IllegalStateException(
                    "La notificación no requiere ninguna acción"
            );
        }

        if (actionedAt != null) {
            throw new IllegalStateException(
                    "La notificación ya ha sido accionada"
            );
        }

        this.actionedAt = LocalDateTime.now();
    }

    public void softDelete() {
        this.active = false;
    }

    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    private static void validateRequiredFields(
            UUID id,
            UUID userId,
            NotificationType type,
            String title,
            LocalDateTime createdAt) {

        if (id == null) {
            throw new IllegalArgumentException(
                    "El id de la notificación es obligatorio"
            );
        }

        if (userId == null) {
            throw new IllegalArgumentException(
                    "El userId de la notificación es obligatorio"
            );
        }

        if (type == null) {
            throw new IllegalArgumentException(
                    "El tipo de notificación es obligatorio"
            );
        }

        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException(
                    "El título de la notificación es obligatorio"
            );
        }

        if (createdAt == null) {
            throw new IllegalArgumentException(
                    "La fecha de creación de la notificación es obligatoria"
            );
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public NotificationType getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public boolean isRead() {
        return read;
    }

    public boolean isActionRequired() {
        return actionRequired;
    }

    public LocalDateTime getActionedAt() {
        return actionedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public boolean isActive() {
        return active;
    }
}