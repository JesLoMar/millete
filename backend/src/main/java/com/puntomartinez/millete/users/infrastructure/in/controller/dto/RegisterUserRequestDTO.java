package com.puntomartinez.millete.users.infrastructure.in.controller.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterUserRequestDTO(

        @Size(max = 50, message = "El nombre de usuario no puede superar los 50 caracteres")
        String username,

        @Email(message = "El formato del email no es válido")
        @Size(max = 100, message = "El email no puede superar los 100 caracteres")
        String email,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, max = 100, message = "La contraseña debe tener entre 8 y 100 caracteres")
        String password
) {
        public RegisterUserRequestDTO {
                username = normalize(username);
                email = normalize(email);
        }

        private static String normalize(String value) {
                return (value == null || value.isBlank()) ? null : value.trim();
        }
}