package com.puntomartinez.millete.family.domain.ports.in;

import com.puntomartinez.millete.family.domain.model.FamilyInvitation;
import java.util.UUID;

public interface InviteFamilyMemberUseCase {
    FamilyInvitation inviteMember(UUID adminUserId, UUID familyId, String guestEmail);
}