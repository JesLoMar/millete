package com.puntomartinez.millete.users.domain.model;

import com.puntomartinez.millete.shared.domain.ports.out.TimeProvider;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class User {
    private final UUID id;
    private String username;
    private String email;
    private String password;
    private final Instant createdAt;
    private Instant modifiedAt;
    private boolean active;
    private boolean anonymized;

    public User(UUID id, String username, String email, String password,
                Instant createdAt, Instant modifiedAt,
                boolean active, boolean anonymized) {

        boolean hasUsername = username != null && !username.isBlank();
        boolean hasEmail = email != null && !email.isBlank();

        if (!hasUsername && !hasEmail) {
            throw new IllegalArgumentException(
                    "El usuario debe tener al menos un email o un nombre de usuario"
            );
        }

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

    public void anonymize(TimeProvider timeProvider) {
        this.username = "user_" + this.id;
        this.email = "anon_" + this.id + "@familybudget.internal";
        this.password = "ANONYMIZED";
        this.anonymized = true;
        this.active = false;
        this.modifiedAt = timeProvider.now();
    }

    public void updateProfile(
            TimeProvider timeProvider,
            String newUsername,
            String newEmail
    ) {
        boolean hasUsername = newUsername != null && !newUsername.isBlank();
        boolean hasEmail = newEmail != null && !newEmail.isBlank();

        if (!hasUsername && !hasEmail) {
            throw new IllegalArgumentException(
                    "El usuario debe tener al menos un email o un nombre de usuario"
            );
        }

        this.username = hasUsername ? newUsername : null;
        this.email = hasEmail ? newEmail : null;
        this.modifiedAt = timeProvider.now();
    }

    public void updatePassword(
            TimeProvider timeProvider,
            String newHashedPassword
    ) {
        if (newHashedPassword == null || newHashedPassword.isBlank()) {
            throw new IllegalArgumentException("La nueva contraseña no es válida");
        }

        this.password = newHashedPassword;
        this.modifiedAt = timeProvider.now();
    }

    public void deactivate(TimeProvider timeProvider) {
        this.active = false;
        this.modifiedAt = timeProvider.now();
    }

    public boolean hasValidIdentity() {
        return (username != null && !username.isBlank())
                || (email != null && !email.isBlank());
    }

    public String getPrimaryIdentifier() {
        if (email != null && !email.isBlank()) {
            return email;
        }
        return username;
    }
}
