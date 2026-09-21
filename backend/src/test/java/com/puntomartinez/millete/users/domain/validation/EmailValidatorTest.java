package com.puntomartinez.millete.users.domain.validation;

import com.puntomartinez.millete.shared.domain.exception.InvalidInputException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("EmailValidator")
class EmailValidatorTest {

    @Nested
    @DisplayName("isValid")
    class IsValid {

        @ParameterizedTest
        @ValueSource(strings = {
                "test@test.com",
                "user.name@domain.co.uk",
                "user+tag@test.com",
                "a@b.co"
        })
        @DisplayName("Should return true for valid emails")
        void shouldReturnTrueForValidEmails(String email) {
            assertThat(EmailValidator.isValid(email)).isTrue();
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "",
                "no-at-sign",
                "no-domain@",
                "@no-local.com",
                "missing-tld@domain",
                "spaces in@email.com"
        })
        @DisplayName("Should return false for invalid emails")
        void shouldReturnFalseForInvalidEmails(String email) {
            assertThat(EmailValidator.isValid(email)).isFalse();
        }

        @Test
        @DisplayName("Should return false for null email")
        void shouldReturnFalseForNull() {
            assertThat(EmailValidator.isValid(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("requireValid")
    class RequireValid {

        @Test
        @DisplayName("Should not throw for valid email")
        void shouldNotThrowForValidEmail() {
            assertThatCode(() ->
                    EmailValidator.requireValid("test@test.com")
            ).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should throw InvalidInputException for invalid email")
        void shouldThrowForInvalidEmail() {
            assertThatThrownBy(() ->
                    EmailValidator.requireValid("not-an-email")
            ).isInstanceOf(InvalidInputException.class);
        }
    }
}