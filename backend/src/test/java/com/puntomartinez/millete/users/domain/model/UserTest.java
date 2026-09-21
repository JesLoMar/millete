package com.puntomartinez.millete.users.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("User aggregate")
class UserTest {

    private static final UUID ID = UUID.randomUUID();
    private static final LocalDateTime NOW = LocalDateTime.now();

    @Nested
    @DisplayName("constructor")
    class Constructor {

        @Test
        @DisplayName("Should create user with username only")
        void shouldCreateUserWithUsernameOnly() {
            User user = new User(
                    ID, "ana", null, "hashed", NOW, NOW, true, false
            );

            assertThat(user.getUsername()).isEqualTo("ana");
            assertThat(user.getEmail()).isNull();
            assertThat(user.getPrimaryIdentifier()).isEqualTo("ana");
        }

        @Test
        @DisplayName("Should create user with email only")
        void shouldCreateUserWithEmailOnly() {
            User user = new User(
                    ID, null, "ana@mail.com", "hashed", NOW, NOW, true, false
            );

            assertThat(user.getEmail()).isEqualTo("ana@mail.com");
            assertThat(user.getUsername()).isNull();
            assertThat(user.getPrimaryIdentifier()).isEqualTo("ana@mail.com");
        }

        @Test
        @DisplayName("Should create user with both username and email")
        void shouldCreateUserWithBoth() {
            User user = new User(
                    ID, "ana", "ana@mail.com", "hashed", NOW, NOW, true, false
            );

            assertThat(user.getUsername()).isEqualTo("ana");
            assertThat(user.getEmail()).isEqualTo("ana@mail.com");
            assertThat(user.isActive()).isTrue();
            assertThat(user.isAnonymized()).isFalse();
        }

        @Test
        @DisplayName("Should reject when no username and no email")
        void shouldRejectWhenNoIdentity() {
            assertThatThrownBy(() ->
                    new User(ID, null, null, "hashed", NOW, NOW, true, false)
            ).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should reject when password is null")
        void shouldRejectWhenPasswordIsNull() {
            assertThatThrownBy(() ->
                    new User(ID, "ana", null, null, NOW, NOW, true, false)
            ).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should reject when password is blank")
        void shouldRejectWhenPasswordIsBlank() {
            assertThatThrownBy(() ->
                    new User(ID, "ana", null, "   ", NOW, NOW, true, false)
            ).isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("anonymize")
    class Anonymize {

        @Test
        @DisplayName("Should anonymize user correctly")
        void shouldAnonymizeCorrectly() {
            User user = new User(
                    ID, "ana", "ana@mail.com", "hashed", NOW, NOW, true, false
            );

            user.anonymize();

            assertThat(user.getUsername()).isEqualTo("user_" + ID);
            assertThat(user.getEmail())
                    .isEqualTo("anon_" + ID + "@familybudget.internal");
            assertThat(user.getPassword()).isEqualTo("ANONYMIZED");
            assertThat(user.isAnonymized()).isTrue();
            assertThat(user.isActive()).isFalse();
            assertThat(user.getModifiedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("updateProfile")
    class UpdateProfile {

        @Test
        @DisplayName("Should update username and email")
        void shouldUpdateUsernameAndEmail() {
            User user = new User(
                    ID, "ana", "ana@mail.com", "hashed", NOW, NOW, true, false
            );

            user.updateProfile("nuevo", "nuevo@mail.com");

            assertThat(user.getUsername()).isEqualTo("nuevo");
            assertThat(user.getEmail()).isEqualTo("nuevo@mail.com");
            assertThat(user.getModifiedAt()).isNotNull();
        }

        @Test
        @DisplayName("Should reject when both new values are blank")
        void shouldRejectWhenBothBlank() {
            User user = new User(
                    ID, "ana", null, "hashed", NOW, NOW, true, false
            );

            assertThatThrownBy(() -> user.updateProfile(null, "   "))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("updatePassword")
    class UpdatePassword {

        @Test
        @DisplayName("Should update password")
        void shouldUpdatePassword() {
            User user = new User(
                    ID, "ana", null, "old", NOW, NOW, true, false
            );

            user.updatePassword("newHashed");

            assertThat(user.getPassword()).isEqualTo("newHashed");
        }

        @Test
        @DisplayName("Should reject null password")
        void shouldRejectNullPassword() {
            User user = new User(
                    ID, "ana", null, "old", NOW, NOW, true, false
            );

            assertThatThrownBy(() -> user.updatePassword(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should reject blank password")
        void shouldRejectBlankPassword() {
            User user = new User(
                    ID, "ana", null, "old", NOW, NOW, true, false
            );

            assertThatThrownBy(() -> user.updatePassword("   "))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("deactivate")
    class Deactivate {

        @Test
        @DisplayName("Should deactivate user")
        void shouldDeactivateUser() {
            User user = new User(
                    ID, "ana", null, "hashed", NOW, NOW, true, false
            );

            user.deactivate();

            assertThat(user.isActive()).isFalse();
            assertThat(user.getModifiedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("hasValidIdentity")
    class HasValidIdentity {

        @Test
        @DisplayName("Should return true with username")
        void shouldReturnTrueWithUsername() {
            User user = new User(
                    ID, "ana", null, "hashed", NOW, NOW, true, false
            );

            assertThat(user.hasValidIdentity()).isTrue();
        }

        @Test
        @DisplayName("Should return true with email")
        void shouldReturnTrueWithEmail() {
            User user = new User(
                    ID, null, "ana@mail.com", "hashed", NOW, NOW, true, false
            );

            assertThat(user.hasValidIdentity()).isTrue();
        }
    }
}