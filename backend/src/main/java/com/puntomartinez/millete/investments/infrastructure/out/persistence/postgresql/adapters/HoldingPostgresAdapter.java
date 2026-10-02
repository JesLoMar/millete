package com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.adapters;

import com.puntomartinez.millete.investments.domain.model.Holding;
import com.puntomartinez.millete.investments.domain.ports.out.HoldingRepository;
import com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.entity.HoldingEntity;
import com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.mappers.HoldingEntityMapper;
import com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.repository.JpaHoldingRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
public final class HoldingPostgresAdapter
        implements HoldingRepository {

    private final JpaHoldingRepository repository;
    private final HoldingEntityMapper mapper;

    public HoldingPostgresAdapter(
            JpaHoldingRepository repository,
            HoldingEntityMapper mapper
    ) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public Holding save(
            Holding holding
    ) {
        if (holding == null) {
            throw new IllegalArgumentException(
                    "El Holding es obligatorio."
            );
        }

        HoldingEntity entity =
                mapper.toEntity(holding);

        HoldingEntity saved =
                repository.save(entity);

        return mapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Holding> findAllByUserId(
            UUID userId
    ) {
        if (userId == null) {
            return List.of();
        }

        return repository
                .findByUserIdOrderBySnapshotAtAscCreatedAtAscIdAsc(
                        userId
                )
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}