package com.puntomartinez.millete.groupgoals.domain.ports.out;

import com.puntomartinez.millete.groupgoals.domain.model.GoalUnit;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GoalUnitRepository {

    GoalUnit save(GoalUnit goalUnit);

    Optional<GoalUnit> findById(UUID id);

    List<GoalUnit> findByIds(Collection<UUID> ids);

    List<GoalUnit> findByUserId(UUID userId, int page, int size);

    long countByUserId(UUID userId);
}