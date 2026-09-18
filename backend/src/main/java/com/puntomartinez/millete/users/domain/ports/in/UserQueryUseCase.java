package com.puntomartinez.millete.users.domain.ports.in;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface UserQueryUseCase {

    Optional<UserInfo> findById(UUID userId);

    Optional<UserInfo> findByIdentifier(String identifier);

    Map<UUID, UserInfo> findByIds(Collection<UUID> userIds);

    record UserInfo(
            UUID id,
            String username,
            String email
    ) {
    }
}