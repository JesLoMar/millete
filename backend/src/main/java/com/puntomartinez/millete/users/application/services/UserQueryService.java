package com.puntomartinez.millete.users.application.services;

import com.puntomartinez.millete.users.domain.model.User;
import com.puntomartinez.millete.users.domain.ports.in.UserQueryUseCase;
import com.puntomartinez.millete.users.domain.ports.out.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserQueryService implements UserQueryUseCase {

    private final UserRepository userRepository;

    public UserQueryService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserInfo> findById(UUID userId) {
        return userRepository.findById(userId)
                .map(this::toUserInfo);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserInfo> findByIdentifier(String identifier) {
        return userRepository.findByIdentifier(identifier)
                .map(this::toUserInfo);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<UUID, UserInfo> findByIds(Collection<UUID> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }

        Map<UUID, UserInfo> result = new HashMap<>();
        for (UUID id : userIds) {
            userRepository.findById(id)
                    .ifPresent(user -> result.put(id, toUserInfo(user)));
        }
        return result;
    }

    private UserInfo toUserInfo(User user) {
        return new UserInfo(
                user.getId(),
                user.getUsername(),
                user.getEmail()
        );
    }
}