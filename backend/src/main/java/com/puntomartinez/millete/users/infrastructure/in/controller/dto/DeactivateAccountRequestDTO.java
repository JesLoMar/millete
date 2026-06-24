package com.puntomartinez.millete.users.infrastructure.in.controller.dto;

import jakarta.validation.constraints.NotBlank;

public record DeactivateAccountRequestDTO(
        @NotBlank(message = "La contraseña es obligatoria para desactivar la cuenta")
        String password
) {}
