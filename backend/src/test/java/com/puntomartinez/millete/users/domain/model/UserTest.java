package com.puntomartinez.millete.users.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.time.LocalDateTime;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;

@DisplayName("User - Modelo de dominio")
class UserTest {

    private final UUID id = UUID.randomUUID();
    private final LocalDateTime now = LocalDateTime.now();

    @Test
    @DisplayName("Debe crear usuario con username")
    void shouldCreateUserWithUsername() {
        User user = new User(id, "ana", null, "hashed", now, now, true, false);

        assertThat(user.getUsername()).isEqualTo("ana");
        assertThat(user.getEmail()).isNull();
        assertThat(user.getPrimaryIdentifier()).isEqualTo("ana");
    }

    @Test
    @DisplayName("Debe crear usuario con email")
    void shouldCreateUserWithEmail() {
        User user = new User(id, null, "ana@mail.com", "hashed", now, now, true, false);

        assertThat(user.getEmail()).isEqualTo("ana@mail.com");
        assertThat(user.getUsername()).isNull();
        assertThat(user.getPrimaryIdentifier()).isEqualTo("ana@mail.com");
    }

    @Test
    @DisplayName("Debe crear usuario con username y email")
    void shouldCreateUserWithBoth() {
        User user = new User(id, "ana", "ana@mail.com", "hashed", now, now, true, false);

        assertThat(user.getUsername()).isEqualTo("ana");
        assertThat(user.getEmail()).isEqualTo("ana@mail.com");
    }

    @Test
    @DisplayName("Debe lanzar error si no hay username ni email")
    void shouldThrowWhenNoIdentity() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new User(id, null, null, "hashed", now, now, true, false))
                .withMessage("El usuario debe tener al menos un email o un nombre de usuario");
    }

    @Test
    @DisplayName("Debe lanzar error si no hay password")
    void shouldThrowWhenNoPassword() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new User(id, "ana", null, null, now, now, true, false))
                .withMessage("La contraseña es obligatoria");
    }

    @Test
    @DisplayName("Debe anonimizar correctamente")
    void shouldAnonymize() {
        User user = new User(id, "ana", "ana@mail.com", "hashed", now, now, true, false);
        user.anonymize();

        assertThat(user.getUsername()).startsWith("user_");
        assertThat(user.getEmail()).contains("@familybudget.internal");
        assertThat(user.getPassword()).isEqualTo("ANONYMIZED");
        assertThat(user.isAnonymized()).isTrue();
        assertThat(user.isActive()).isFalse();
    }

    @Test
    @DisplayName("Debe actualizar password")
    void shouldUpdatePassword() {
        User user = new User(id, "ana", null, "old", now, now, true, false);
        user.updatePassword("new");

        assertThat(user.getPassword()).isEqualTo("new");
        assertThat(user.getModifiedAt()).isAfterOrEqualTo(now);
    }

    @Test
    @DisplayName("Debe desactivar usuario")
    void shouldDeactivate() {
        User user = new User(id, "ana", null, "hashed", now, now, true, false);
        user.deactivate();

        assertThat(user.isActive()).isFalse();
        assertThat(user.getModifiedAt()).isAfterOrEqualTo(now);
    }

    @Test
    @DisplayName("hasValidIdentity con username")
    void shouldHaveValidIdentityWithUsername() {
        User user = new User(id, "ana", null, "hashed", now, now, true, false);
        assertThat(user.hasValidIdentity()).isTrue();
    }

    @Test
    @DisplayName("getPrimaryIdentifier con email")
    void shouldReturnEmailAsPrimary() {
        User user = new User(id, "ana", "ana@mail.com", "hashed", now, now, true, false);
        assertThat(user.getPrimaryIdentifier()).isEqualTo("ana@mail.com");
    }
}