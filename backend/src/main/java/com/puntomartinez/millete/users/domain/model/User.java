package com.puntomartinez.millete.users.domain.model;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class User {
    private final UUID id;
    private String username;
    private String email;
    private String password;
    private final LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private boolean active;
    private boolean anonymized;

    public User(UUID id, String username, String email, String password,
                LocalDateTime createdAt, LocalDateTime modifiedAt,
                boolean active, boolean anonymized) {

        // 1. NUEVA VALIDACIÓN HÍBRIDA: Verificamos qué datos nos llegan
        boolean hasUsername = username != null && !username.isBlank();
        boolean hasEmail = email != null && !email.isBlank();

        if (!hasUsername && !hasEmail) {
            throw new IllegalArgumentException("El usuario debe tener al menos un email o un nombre de usuario");
        }

        // 2. Mantenemos tu validación de contraseña (si la tenías)
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("La contraseña es obligatoria");
        }

        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.createdAt = createdAt;
        this.modifiedAt = modifiedAt;
        this.active = active;
        this.anonymized = anonymized;
    }

    // ==========================================
    // LÓGICA DE NEGOCIO Y SEGURIDAD
    // ==========================================

    public void anonymize() {
        this.username = "user_" + this.id.toString().substring(0, 8);
        this.email = "anon_" + this.id + "@familybudget.internal";
        this.password = "ANONYMIZED";
        this.anonymized = true;
        this.active = false;
        this.modifiedAt = LocalDateTime.now();
    }

    public void updatePassword(String newHashedPassword) {
        if (newHashedPassword == null || newHashedPassword.isBlank()) {
            throw new IllegalArgumentException("La nueva contraseña no es válida");
        }
        this.password = newHashedPassword;
        this.modifiedAt = LocalDateTime.now();
    }

    public void deactivate() {
        this.active = false;
        this.modifiedAt = LocalDateTime.now();
    }

    public boolean hasValidIdentity() {
        return (username != null && !username.isBlank()) || (email != null && !email.isBlank());
    }

    public String getPrimaryIdentifier() {
        if (email != null && !email.isBlank()) {
            return email;
        }
        return username;
    }
}