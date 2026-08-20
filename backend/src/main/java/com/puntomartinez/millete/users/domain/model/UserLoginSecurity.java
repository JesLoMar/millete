package com.puntomartinez.millete.users.domain.model;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.UUID;
@Getter
@Setter
public class UserLoginSecurity {
    private UUID userId;
    private int failedAttempts;
    private LocalDateTime blockedUntil;
    private LocalDateTime lastAttemptAt;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    public UserLoginSecurity() {}
    public boolean isBlocked() {
        if (this.blockedUntil != null) {
            if (LocalDateTime.now().isAfter(this.blockedUntil)) {
                this.failedAttempts = 0;
                this.blockedUntil = null;
                this.modifiedAt = LocalDateTime.now();
                return false;
            }
            return true;
        }
        return false;
    }
    public void registerFailedAttempt(int maxAttempts, long lockDurationMinutes) {
        this.failedAttempts++;
        this.lastAttemptAt = LocalDateTime.now();
        this.modifiedAt = LocalDateTime.now();
        if (this.failedAttempts >= maxAttempts) {
            this.blockedUntil = LocalDateTime.now().plusMinutes(lockDurationMinutes);
        }
    }
    public void resetAttempts() {
        if (this.failedAttempts > 0 || this.blockedUntil != null) {
            this.failedAttempts = 0;
            this.blockedUntil = null;
            this.lastAttemptAt = LocalDateTime.now();
            this.modifiedAt = LocalDateTime.now();
        }
    }
}