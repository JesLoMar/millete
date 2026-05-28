package com.puntomartinez.millete.family.domain.ports.out;

import com.puntomartinez.millete.family.domain.model.FamilyUnit;
import java.util.Optional;
import java.util.UUID;

public interface FamilyUnitRepository {
    FamilyUnit save(FamilyUnit familyUnit);
    Optional<FamilyUnit> findById(UUID id);
    void deleteById(UUID id);
}