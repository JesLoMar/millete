package com.puntomartinez.millete.notifications.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    private UUID id;
    private UUID userId;
    private NotificationType type;
    private String title;
    private String message;
    private Map<String, Object> metadata;
    private boolean read;
    private boolean actionRequired;
    private LocalDateTime actionedAt;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private boolean active;

    public void markAsRead() {
        this.read = true;
    }

    public void markAsActioned() {
        this.actionedAt = LocalDateTime.now();
    }

    public void softDelete() {
        this.active = false;
    }

    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
}
