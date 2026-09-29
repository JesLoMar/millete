package com.puntomartinez.millete.investments.domain.ports.out;

import com.puntomartinez.millete.investments.domain.model.CurrencyCode;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface UserCurrencyPort {

    Optional<CurrencyCode> currencyAt(UUID userId, Instant at);
}