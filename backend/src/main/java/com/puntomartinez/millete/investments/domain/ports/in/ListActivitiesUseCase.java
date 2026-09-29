package com.puntomartinez.millete.investments.domain.ports.in;

import com.puntomartinez.millete.investments.domain.model.Activity;
import com.puntomartinez.millete.investments.domain.model.AssetReference;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ListActivitiesUseCase {

    List<Activity> list(
            UUID userId,
            AssetReference assetReference,
            Instant from,
            Instant to
    );
}