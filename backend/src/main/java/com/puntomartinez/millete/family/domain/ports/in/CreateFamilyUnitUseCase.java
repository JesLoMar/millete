package com.puntomartinez.millete.family.domain.ports.in;

import com.puntomartinez.millete.family.domain.model.FamilyUnit;
import java.math.BigDecimal;
import java.util.UUID;

public interface CreateFamilyUnitUseCase {
    FamilyUnit createFamilyUnit(UUID adminUserId, String name, BigDecimal monthlyTarget, String distributionMode);
}