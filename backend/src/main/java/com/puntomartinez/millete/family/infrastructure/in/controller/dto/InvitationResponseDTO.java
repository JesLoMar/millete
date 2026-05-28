package com.puntomartinez.millete.family.infrastructure.in.controller.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;

@Data
public class InvitationResponseDTO {
    private UUID id;
    private String email;
    private String status;
    private LocalDateTime expiresAt;
}