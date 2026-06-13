package com.puntomartinez.millete.users.domain.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class UserSession {
    private UUID id;
    private UUID userId;
    private String channel;
    private Long telegramChatId;
    private int loginAttempts;
    private LocalDateTime blockedUntil;
    private LocalDateTime lastAttemptAt;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    public UserSession() {}

    public boolean isBlocked() {
        if (this.blockedUntil != null) {
            if (LocalDateTime.now().isAfter(this.blockedUntil)) {
                this.loginAttempts = 0;
                this.blockedUntil = null;
                this.modifiedAt = LocalDateTime.now();
                return false;
            }
            return true;
        }
        return false;
    }

    public void registerFailedAttempt(int maxAttempts, long lockDurationMinutes) {
        this.loginAttempts++;
        this.lastAttemptAt = LocalDateTime.now();
        this.modifiedAt = LocalDateTime.now();

        if (this.loginAttempts >= maxAttempts) {
            this.blockedUntil = LocalDateTime.now().plusMinutes(lockDurationMinutes);
        }
    }

    public void resetAttempts() {
        if (this.loginAttempts > 0 || this.blockedUntil != null) {
            this.loginAttempts = 0;
            this.blockedUntil = null;
            this.lastAttemptAt = LocalDateTime.now();
            this.modifiedAt = LocalDateTime.now();
        }
    }
}