package com.puntomartinez.millete.investments.domain.ports.out;

import com.puntomartinez.millete.investments.domain.model.Holding;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HoldingRepository {

    Holding save(Holding holding);

    Optional<Holding> findByIdAndUserId(
            UUID holdingId,
            UUID userId
    );

    List<Holding> findAllByUserId(UUID userId);
}