package com.puntomartinez.millete.users.infrastructure.in.controller.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateProfileRequestDTO(
        String newUsername,
        String newEmail,
        @NotBlank(message = "La contraseña actual es obligatoria para actualizar el perfil")
        String currentPassword
) {}
