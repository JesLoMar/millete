package com.puntomartinez.millete.users.domain.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class UserSession {

    public static final String CHANNEL_WEB = "WEB";

    private UUID id;
    private UUID userId;
    private String channel;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private boolean active;

    public UserSession() {
    }
}