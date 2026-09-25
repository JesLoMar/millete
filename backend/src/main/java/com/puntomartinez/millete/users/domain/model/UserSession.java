package com.puntomartinez.millete.users.domain.model;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class UserSession {

    public static final String CHANNEL_WEB = "WEB";

    private UUID id;
    private UUID userId;
    private String channel;
    private Instant createdAt;
    private Instant modifiedAt;
    private boolean active;

    public UserSession() {
    }
}