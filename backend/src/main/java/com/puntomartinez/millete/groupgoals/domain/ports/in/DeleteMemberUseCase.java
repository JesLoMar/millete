package com.puntomartinez.millete.groupgoals.domain.ports.in;

import java.util.UUID;

public interface DeleteMemberUseCase {

    void deleteMember(
            UUID goalId,
            UUID memberId,
            UUID userId
    );
}