package com.puntomartinez.millete.family.domain.ports.out;

import com.puntomartinez.millete.family.domain.model.FamilyContribution;
import java.util.List;
import java.util.UUID;

public interface FamilyContributionRepository {
    FamilyContribution save(FamilyContribution contribution);
    List<FamilyContribution> findByFamilyId(UUID familyId);
}