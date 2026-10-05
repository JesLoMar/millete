package com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.adapters;

import com.puntomartinez.millete.investments.domain.model.Activity;
import com.puntomartinez.millete.investments.domain.model.AssetReference;
import com.puntomartinez.millete.investments.domain.ports.out.ActivityRepository;
import com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.entity.ActivityEntity;
import com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.mappers.ActivityEntityMapper;
import com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.repository.JpaActivityRepository;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public final class ActivityPostgresAdapter
implements ActivityRepository {

private final JpaActivityRepository repository;
private final ActivityEntityMapper mapper;

public ActivityPostgresAdapter(
        JpaActivityRepository repository,
        ActivityEntityMapper mapper
) {
    this.repository = repository;
    this.mapper = mapper;
}

@Override
public Activity save(
        Activity activity
) {
    if (activity == null) {
        throw new IllegalArgumentException(
                "La Activity es obligatoria."
        );
    }

    ActivityEntity entity =
            mapper.toEntity(activity);

    ActivityEntity savedEntity =
            repository.save(entity);

    return mapper.toDomain(savedEntity);
}

@Override
public Optional<Activity> findByIdAndUserId(
        UUID activityId,
        UUID userId
) {
    if (activityId == null
            || userId == null) {
        return Optional.empty();
    }

    return repository.findByIdAndUserId(
                    activityId,
                    userId
            )
            .map(mapper::toDomain);
}

@Override
public List<Activity> findAllByUserId(
        UUID userId
) {
    if (userId == null) {
        return List.of();
    }

    return repository
            .findByUserIdOrderByOccurredAtAscOrderingKeyAscIdAsc(
                    userId
            )
            .stream()
            .map(mapper::toDomain)
            .toList();
}

@Override
public List<Activity> findByUserIdAndFilters(
        UUID userId,
        AssetReference assetReference,
        Instant from,
        Instant to
) {
    if (userId == null) {
        return List.of();
    }

    if (from != null
            && to != null
            && from.isAfter(to)) {
        throw new IllegalArgumentException(
                "El rango temporal de Activities no es válido."
        );
    }

    Specification<ActivityEntity> specification =
            (root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(
                            root.get("userId"),
                            userId
                    );

    if (assetReference != null) {
        specification = specification.and(
                (root, query, criteriaBuilder) ->
                        criteriaBuilder.and(
                                criteriaBuilder.equal(
                                        root.get("assetReferenceKind"),
                                        assetReference.kind().name()
                                ),
                                criteriaBuilder.equal(
                                        root.get("assetReferenceId"),
                                        assetReference.id()
                                )
                        )
        );
    }

    if (from != null) {
        specification = specification.and(
                (root, query, criteriaBuilder) ->
                        criteriaBuilder.greaterThanOrEqualTo(
                                root.get("occurredAt"),
                                from
                        )
        );
    }

    if (to != null) {
        specification = specification.and(
                (root, query, criteriaBuilder) ->
                        criteriaBuilder.lessThanOrEqualTo(
                                root.get("occurredAt"),
                                to
                        )
        );
    }

    return repository.findAll(
                    specification,
                    Sort.by(
                            Sort.Order.asc("occurredAt"),
                            Sort.Order.asc("orderingKey"),
                            Sort.Order.asc("id")
                    )
            )
            .stream()
            .map(mapper::toDomain)
            .toList();
}

}