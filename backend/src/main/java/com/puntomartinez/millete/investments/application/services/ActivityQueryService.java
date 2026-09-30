package com.puntomartinez.millete.investments.application.services;

import com.puntomartinez.millete.investments.domain.model.Activity;
import com.puntomartinez.millete.investments.domain.model.AssetReference;
import com.puntomartinez.millete.investments.domain.ports.in.ListActivitiesUseCase;
import com.puntomartinez.millete.investments.domain.ports.out.ActivityRepository;
import com.puntomartinez.millete.shared.domain.exception.InvalidInputException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class ActivityQueryService implements ListActivitiesUseCase {

    private final ActivityRepository activities;

    public ActivityQueryService(
            ActivityRepository activities
    ) {
        this.activities = activities;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Activity> list(
            UUID userId,
            AssetReference assetReference,
            Instant from,
            Instant to
    ) {
        validateRange(from, to);

        return activities
                .findByUserIdAndFilters(
                        userId,
                        assetReference,
                        from,
                        to
                )
                .stream()
                .sorted(activityOrder())
                .toList();
    }

    private void validateRange(
            Instant from,
            Instant to
    ) {
        if (from != null
                && to != null
                && from.isAfter(to)) {

            throw new InvalidInputException(
                    "El rango de fechas de las Activities no es válido."
            );
        }
    }

    private Comparator<Activity> activityOrder() {
        return Comparator
                .comparing(Activity::getOccurredAt)
                .thenComparingLong(Activity::getOrderingKey)
                .thenComparing(activity ->
                        activity.getId().toString()
                );
    }
}