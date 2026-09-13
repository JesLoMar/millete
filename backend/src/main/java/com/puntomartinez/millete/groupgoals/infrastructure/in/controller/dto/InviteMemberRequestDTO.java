package com.puntomartinez.millete.groupgoals.infrastructure.in.controller.dto;

import jakarta.validation.constraints.NotBlank;

public record InviteMemberRequestDTO(

        @NotBlank(message = "El identificador del usuario es obligatorio.")
        String identifier

) {
}