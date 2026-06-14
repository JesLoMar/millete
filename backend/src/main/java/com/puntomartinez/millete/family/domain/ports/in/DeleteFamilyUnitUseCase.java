package com.puntomartinez.millete.family.domain.ports.in;

import java.util.UUID;

public interface DeleteFamilyUnitUseCase {
    void deleteFamily(UUID familyId, UUID userId);
}