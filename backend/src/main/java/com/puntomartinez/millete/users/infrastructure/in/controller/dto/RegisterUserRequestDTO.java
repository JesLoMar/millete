package com.puntomartinez.millete.users.infrastructure.in.controller.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterUserRequestDTO(

        @NotBlank(message = "El nombre de usuario es obligatorio")
        @Size(max = 50, message = "El nombre de usuario no puede exceder 50 caracteres")
        String username,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El formato del email no es válido")
        String email,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, max = 100, message = "La contraseña debe tener entre 8 y 100 caracteres")
        String password
) {}