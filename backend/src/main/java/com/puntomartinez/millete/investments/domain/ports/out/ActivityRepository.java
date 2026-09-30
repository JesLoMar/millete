package com.puntomartinez.millete.investments.domain.ports.out;

import com.puntomartinez.millete.investments.domain.model.Activity;
import com.puntomartinez.millete.investments.domain.model.ActivityType;
import com.puntomartinez.millete.investments.domain.model.AssetReference;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ActivityRepository {

    Activity save(Activity activity);

    Optional<Activity> findByIdAndUserId(
            UUID activityId,
            UUID userId
    );

    List<Activity> findAllByUserId(UUID userId);

    List<Activity> findByUserIdAndFilters(
            UUID userId,
            AssetReference assetReference,
            Instant from,
            Instant to
    );
}