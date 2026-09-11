package com.puntomartinez.millete.users.infrastructure.out.persistence.postgresql.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;
@Entity
@Table(name = "user_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSessionEntity {
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    @Column(name = "channel", nullable = false, length = 20)
    private String channel;
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "active", nullable = false)
    private boolean active;
    @Column(name = "modified_at", nullable = false)
    private LocalDateTime modifiedAt;
}