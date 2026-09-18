package com.puntomartinez.millete.groupgoals.infrastructure.out.users;

import com.puntomartinez.millete.groupgoals.domain.ports.out.UserLookupPort;
import com.puntomartinez.millete.users.domain.ports.in.UserQueryUseCase;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
public class UserLookupAdapter implements UserLookupPort {

    private final UserQueryUseCase userQueryUseCase;

    public UserLookupAdapter(UserQueryUseCase userQueryUseCase) {
        this.userQueryUseCase = userQueryUseCase;
    }

    @Override
    public Optional<UserLookupPort.UserInfo> findById(UUID userId) {
        return userQueryUseCase.findById(userId)
                .map(this::toDomain);
    }

    @Override
    public Optional<UserLookupPort.UserInfo> findByIdentifier(String identifier) {
        return userQueryUseCase.findByIdentifier(identifier)
                .map(this::toDomain);
    }

    @Override
    public Map<UUID, UserLookupPort.UserInfo> findByIds(Collection<UUID> userIds) {
        Map<UUID, UserQueryUseCase.UserInfo> external =
                userQueryUseCase.findByIds(userIds);
        Map<UUID, UserLookupPort.UserInfo> result =
                new HashMap<>(external.size());
        external.forEach((id, info) -> result.put(id, toDomain(info)));
        return result;
    }

    private UserLookupPort.UserInfo toDomain(UserQueryUseCase.UserInfo external) {
        return new UserLookupPort.UserInfo(
                external.id(),
                external.username(),
                external.email()
        );
    }
}