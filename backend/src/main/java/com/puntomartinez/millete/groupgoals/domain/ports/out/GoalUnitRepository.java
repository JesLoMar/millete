package com.puntomartinez.millete.groupgoals.domain.ports.out;

import com.puntomartinez.millete.groupgoals.domain.model.GoalUnit;
import java.util.Optional;
import java.util.UUID;

public interface GoalUnitRepository {
    GoalUnit save(GoalUnit goalUnit);
    Optional<GoalUnit> findById(UUID id);
    void deleteById(UUID id);
}
