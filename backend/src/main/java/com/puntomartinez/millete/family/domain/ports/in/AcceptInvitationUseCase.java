package com.puntomartinez.millete.family.domain.ports.in;

import com.puntomartinez.millete.family.domain.model.FamilyInvitation;

import java.util.UUID;

public interface AcceptInvitationUseCase {
    FamilyInvitation acceptInvitation(UUID userId, String token);
}