package com.puntomartinez.millete.users.application.services;

import com.puntomartinez.millete.shared.domain.ports.out.TimeProvider;
import com.puntomartinez.millete.users.domain.model.UserPreferences;
import com.puntomartinez.millete.users.domain.ports.out.UserPreferencesRepository;
import org.springframework.stereotype.Service;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

/** Resolves the current calendar date using a user's saved IANA time zone. */
@Service
public class UserLocalDateService {

    private static final String TIMEZONE_PREFERENCE = "timezone";

    private final UserPreferencesRepository userPreferencesRepository;
    private final TimeProvider timeProvider;

    public UserLocalDateService(
            UserPreferencesRepository userPreferencesRepository,
            TimeProvider timeProvider
    ) {
        this.userPreferencesRepository = userPreferencesRepository;
        this.timeProvider = timeProvider;
    }

    public LocalDate todayFor(UUID userId) {
        ZoneId zone = userPreferencesRepository.findByUserId(userId)
                .map(UserPreferences::getPreferences)
                .map(preferences -> preferences.get(TIMEZONE_PREFERENCE))
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .map(this::parseZone)
                .orElse(timeProvider.zone());

        return timeProvider.todayFor(zone);
    }

    private ZoneId parseZone(String timezone) {
        try {
            return ZoneId.of(timezone);
        } catch (DateTimeException exception) {
            return timeProvider.zone();
        }
    }
}
