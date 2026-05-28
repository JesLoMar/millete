package com.puntomartinez.millete.family.domain.ports.out;

import com.puntomartinez.millete.family.domain.model.FamilyMember;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FamilyMemberRepository {
    FamilyMember save(FamilyMember familyMember);
    Optional<FamilyMember> findById(UUID id);
    Optional<FamilyMember> findByFamilyIdAndUserId(UUID familyId, UUID userId);
    List<FamilyMember> findByFamilyId(UUID familyId);
    void deleteByFamilyIdAndUserId(UUID familyId, UUID userId);
    List<FamilyMember> findByUserId(UUID userId);
}