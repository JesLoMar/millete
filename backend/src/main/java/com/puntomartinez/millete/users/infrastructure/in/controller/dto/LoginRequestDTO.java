package com.puntomartinez.millete.users.infrastructure.in.controller.dto;
import jakarta.validation.constraints.NotBlank;
public record LoginRequestDTO(
        @NotBlank(message = "Debes indicar tu email o nombre de usuario")
        String identifier,
        @NotBlank(message = "La contraseña es obligatoria")
        String password
) {}