package com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.adapters;

import com.puntomartinez.millete.investments.domain.model.AssetPrice;
import com.puntomartinez.millete.investments.domain.ports.out.AssetPriceRepository;
import com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.entity.AssetPriceEntity;
import com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.mappers.AssetPriceEntityMapper;
import com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.repository.JpaAssetPriceRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public final class AssetPricePostgresAdapter
        implements AssetPriceRepository {

    private final JpaAssetPriceRepository repository;
    private final AssetPriceEntityMapper mapper;

    public AssetPricePostgresAdapter(
            JpaAssetPriceRepository repository,
            AssetPriceEntityMapper mapper
    ) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public void saveAll(
            List<AssetPrice> prices
    ) {
        if (prices == null || prices.isEmpty()) {
            return;
        }

        for (AssetPrice price : prices) {
            if (price == null) {
                throw new IllegalArgumentException(
                        "La lista de precios no puede contener elementos nulos."
                );
            }

            AssetPriceEntity entity =
                    mapper.toEntity(price);

            repository.upsert(
                    entity.getId(),
                    entity.getSharedAssetId(),
                    entity.getTimestamp(),
                    entity.getOpen(),
                    entity.getHigh(),
                    entity.getLow(),
                    entity.getClose(),
                    entity.getAdjustedClose(),
                    entity.getVolume(),
                    entity.getCurrency(),
                    entity.getSource(),
                    entity.getFetchedAt()
            );
        }
    }

    @Override
    public Optional<AssetPrice> findLatestAt(
            UUID sharedAssetId,
            Instant at
    ) {
        if (sharedAssetId == null || at == null) {
            return Optional.empty();
        }

        return repository.findLatestCandidates(
                        sharedAssetId,
                        at
                )
                .stream()
                .findFirst()
                .map(mapper::toDomain);
    }

    @Override
    public List<AssetPrice> findBySharedAssetIdAndTimestampBetween(
            UUID sharedAssetId,
            Instant from,
            Instant to
    ) {
        if (sharedAssetId == null) {
            return List.of();
        }

        if (from == null
                || to == null
                || from.isAfter(to)) {
            throw new IllegalArgumentException(
                    "El rango temporal de precios no es válido."
            );
        }

        return repository
                .findBySharedAssetIdAndTimestampBetweenOrderByTimestampAscFetchedAtAscSourceAscIdAsc(
                        sharedAssetId,
                        from,
                        to
                )
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}