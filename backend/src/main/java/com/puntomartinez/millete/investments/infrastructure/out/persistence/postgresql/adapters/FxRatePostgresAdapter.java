package com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.adapters;

import com.puntomartinez.millete.investments.domain.model.CurrencyCode;
import com.puntomartinez.millete.investments.domain.model.FxRate;
import com.puntomartinez.millete.investments.domain.ports.out.FxRateRepository;
import com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.entity.FxRateEntity;
import com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.mappers.FxRateEntityMapper;
import com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.repository.JpaFxRateRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Component
public final class FxRatePostgresAdapter
        implements FxRateRepository {

    private final JpaFxRateRepository repository;
    private final FxRateEntityMapper mapper;

    public FxRatePostgresAdapter(
            JpaFxRateRepository repository,
            FxRateEntityMapper mapper
    ) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public void saveAll(
            List<FxRate> rates
    ) {
        if (rates == null || rates.isEmpty()) {
            return;
        }

        for (FxRate rate : rates) {
            if (rate == null) {
                throw new IllegalArgumentException(
                        "La lista de tipos de cambio no puede contener elementos nulos."
                );
            }

            FxRateEntity entity =
                    mapper.toEntity(rate);

            repository.upsert(
                    entity.getId(),
                    entity.getBaseCurrency(),
                    entity.getQuoteCurrency(),
                    entity.getTimestamp(),
                    entity.getRate(),
                    entity.getSource(),
                    entity.getFetchedAt()
            );
        }
    }

    @Override
    public Optional<FxRate> findLatestAt(
            CurrencyCode baseCurrency,
            CurrencyCode quoteCurrency,
            Instant at
    ) {
        validateCurrencies(
                baseCurrency,
                quoteCurrency
        );

        if (at == null) {
            return Optional.empty();
        }

        return repository.findLatestCandidates(
                        baseCurrency.value(),
                        quoteCurrency.value(),
                        at
                )
                .stream()
                .findFirst()
                .map(mapper::toDomain);
    }

    @Override
    public List<FxRate> findByCurrenciesAndTimestampBetween(
            CurrencyCode baseCurrency,
            CurrencyCode quoteCurrency,
            Instant from,
            Instant to
    ) {
        validateCurrencies(
                baseCurrency,
                quoteCurrency
        );

        if (from == null
                || to == null
                || from.isAfter(to)) {
            throw new IllegalArgumentException(
                    "El rango temporal de FX no es válido."
            );
        }

        return repository
                .findByBaseCurrencyAndQuoteCurrencyAndTimestampBetweenOrderByTimestampAscFetchedAtAscSourceAscIdAsc(
                        baseCurrency.value(),
                        quoteCurrency.value(),
                        from,
                        to
                )
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    private void validateCurrencies(
            CurrencyCode baseCurrency,
            CurrencyCode quoteCurrency
    ) {
        if (baseCurrency == null
                || quoteCurrency == null) {
            throw new IllegalArgumentException(
                    "Las monedas del FX son obligatorias."
            );
        }

        if (baseCurrency.equals(quoteCurrency)) {
            throw new IllegalArgumentException(
                    "Las monedas del FX deben ser distintas."
            );
        }
    }
}