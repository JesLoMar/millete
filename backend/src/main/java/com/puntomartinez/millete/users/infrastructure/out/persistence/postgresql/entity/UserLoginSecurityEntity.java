package com.puntomartinez.millete.users.infrastructure.out.persistence.postgresql.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;
@Entity
@Table(name = "user_login_security")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserLoginSecurityEntity {
    @Id
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    @Column(name = "failed_attempts", nullable = false)
    private int failedAttempts;
    @Column(name = "blocked_until")
    private LocalDateTime blockedUntil;
    @Column(name = "last_attempt_at")
    private LocalDateTime lastAttemptAt;
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "modified_at", nullable = false)
    private LocalDateTime modifiedAt;
}