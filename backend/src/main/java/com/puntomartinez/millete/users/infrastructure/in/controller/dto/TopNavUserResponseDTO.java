package com.puntomartinez.millete.users.infrastructure.in.controller.dto;

import java.util.UUID;

public record TopNavUserResponseDTO(
        String username,
        String email,
        UUID sessionId
) {}
