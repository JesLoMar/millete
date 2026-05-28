package com.puntomartinez.millete.family.domain.ports.out;

import com.puntomartinez.millete.family.domain.model.FamilyInvitation;
import com.puntomartinez.millete.family.domain.model.InvitationStatus;

import java.util.Optional;
import java.util.UUID;

public interface FamilyInvitationRepository {
    FamilyInvitation save(FamilyInvitation familyInvitation);
    Optional<FamilyInvitation> findByToken(String token);
    Optional<FamilyInvitation> findById(UUID id);
    Optional<FamilyInvitation> findByFamilyIdAndEmailAndStatus(UUID familyId, String email, InvitationStatus status);
}