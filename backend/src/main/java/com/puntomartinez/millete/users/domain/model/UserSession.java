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
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private boolean active;
    public UserSession() {}
}