package com.puntomartinez.millete.users.application.services;

import com.puntomartinez.millete.shared.domain.ports.out.TimeProvider;
import com.puntomartinez.millete.users.domain.ports.in.ManageProfileUseCase;
import com.puntomartinez.millete.users.domain.ports.in.RegisterUserUseCase.RegisterUserCommand;
import com.puntomartinez.millete.users.domain.ports.out.PasswordHasherPort;
import com.puntomartinez.millete.users.domain.ports.out.UserRepository;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Map;

import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserRegistrationPreferencesTest {
    @Mock UserRepository userRepository;
    @Mock PasswordHasherPort passwordHasher;
    @Mock AccountLockService accountLockService;
    @Mock TimeProvider timeProvider;
    @Mock ManageProfileUseCase manageProfile;
    @InjectMocks UserService userService;

    @ParameterizedTest
    @CsvSource({"Europe/Madrid, Europe/Madrid", ", UTC", "Invalid/Timezone, UTC"})
    void initializesEuroAndTimezone(String requestedTimezone, String expectedTimezone) {
        when(timeProvider.now()).thenReturn(Instant.parse("2026-10-10T12:00:00Z"));
        when(passwordHasher.hashPassword("password123")).thenReturn("hashedPassword");
        when(userRepository.saveNewUser(any())).thenAnswer(returnsFirstArg());

        var user = userService.register(requestedTimezone == null
                ? new RegisterUserCommand("ana", null, "password123")
                : new RegisterUserCommand("ana", null, "password123", requestedTimezone));

        var persistence = inOrder(userRepository, manageProfile);
        persistence.verify(userRepository).saveNewUser(user);
        persistence.verify(manageProfile).updatePreferences(user.getId(),
                Map.of("localCurrency", "EUR", "timezone", expectedTimezone));
    }
}
