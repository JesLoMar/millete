package com.puntomartinez.millete.users.infrastructure.in.controller.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequestDTO(
        @Size(max = 50, message = "El nombre de usuario no puede superar los 50 caracteres")
        String newUsername,

        @Email(message = "El formato del email no es válido")
        @Size(max = 100, message = "El email no puede superar los 100 caracteres")
        String newEmail,

        @NotBlank(message = "La contraseña actual es obligatoria para actualizar el perfil")
        String currentPassword
) {
        public UpdateProfileRequestDTO {
                newUsername = normalize(newUsername);
                newEmail = normalize(newEmail);
        }

        private static String normalize(String value) {
                return (value == null || value.isBlank()) ? null : value.trim();
        }
}