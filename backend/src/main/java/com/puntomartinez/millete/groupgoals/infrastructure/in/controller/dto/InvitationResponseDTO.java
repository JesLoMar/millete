package com.puntomartinez.millete.groupgoals.infrastructure.in.controller.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class InvitationResponseDTO {
    private UUID id;
    private UUID familyId;
    private String familyName;
    private UUID inviterUserId;
    private String inviterName;
    private UUID invitedUserId;
    private String status;
    private LocalDateTime createdAt;
}
