package com.puntomartinez.millete.groupgoals.infrastructure.out.users;

import com.puntomartinez.millete.groupgoals.domain.ports.out.UserLookupPort;
import com.puntomartinez.millete.users.infrastructure.out.persistence.postgresql.entity.UserEntity;
import com.puntomartinez.millete.users.infrastructure.out.persistence.postgresql.repository.JpaUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserLookupPostgresAdapter implements UserLookupPort {

    private final JpaUserRepository jpaUserRepository;

    @Override
    public Optional<UserInfo> findById(UUID userId) {
        return jpaUserRepository.findById(userId)
                .map(this::toUserInfo);
    }

    @Override
    public Optional<UserInfo> findByIdentifier(String identifier) {
        return jpaUserRepository
                .findByUsernameOrEmail(identifier, identifier)
                .map(this::toUserInfo);
    }

    @Override
    public Map<UUID, UserInfo> findByIds(Collection<UUID> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }

        return jpaUserRepository.findAllById(userIds)
                .stream()
                .map(this::toUserInfo)
                .collect(Collectors.toMap(
                        UserInfo::id,
                        Function.identity()
                ));
    }

    private UserInfo toUserInfo(UserEntity user) {
        return new UserInfo(
                user.getId(),
                user.getUsername(),
                user.getEmail()
        );
    }
}