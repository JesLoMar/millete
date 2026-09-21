package com.puntomartinez.millete.users.application.services;

import com.puntomartinez.millete.users.domain.model.User;
import com.puntomartinez.millete.users.domain.ports.in.UserQueryUseCase;
import com.puntomartinez.millete.users.domain.ports.out.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserQueryService")
class UserQueryServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserQueryService userQueryService;

    private User createUser(UUID id, String username, String email) {
        return new User(
                id, username, email, "hashed",
                LocalDateTime.now(), LocalDateTime.now(), true, false
        );
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("Should return UserInfo when user exists")
        void shouldReturnUserInfoWhenExists() {
            UUID id = UUID.randomUUID();
            User user = createUser(id, "ana", "ana@mail.com");
            when(userRepository.findById(id))
                    .thenReturn(Optional.of(user));

            Optional<UserQueryUseCase.UserInfo> result =
                    userQueryService.findById(id);

            assertThat(result).isPresent();
            assertThat(result.get().id()).isEqualTo(id);
            assertThat(result.get().username()).isEqualTo("ana");
            assertThat(result.get().email()).isEqualTo("ana@mail.com");
        }

        @Test
        @DisplayName("Should return empty when user not found")
        void shouldReturnEmptyWhenNotFound() {
            UUID id = UUID.randomUUID();
            when(userRepository.findById(id))
                    .thenReturn(Optional.empty());

            Optional<UserQueryUseCase.UserInfo> result =
                    userQueryService.findById(id);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByIdentifier")
    class FindByIdentifier {

        @Test
        @DisplayName("Should return UserInfo when identifier matches")
        void shouldReturnUserInfoWhenIdentifierMatches() {
            UUID id = UUID.randomUUID();
            User user = createUser(id, "ana", "ana@mail.com");
            when(userRepository.findByIdentifier("ana@mail.com"))
                    .thenReturn(Optional.of(user));

            Optional<UserQueryUseCase.UserInfo> result =
                    userQueryService.findByIdentifier("ana@mail.com");

            assertThat(result).isPresent();
            assertThat(result.get().id()).isEqualTo(id);
        }

        @Test
        @DisplayName("Should return empty when identifier not found")
        void shouldReturnEmptyWhenIdentifierNotFound() {
            when(userRepository.findByIdentifier("unknown"))
                    .thenReturn(Optional.empty());

            Optional<UserQueryUseCase.UserInfo> result =
                    userQueryService.findByIdentifier("unknown");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByIds")
    class FindByIds {

        @Test
        @DisplayName("Should return map of found users")
        void shouldReturnMapOfFoundUsers() {
            UUID id1 = UUID.randomUUID();
            UUID id2 = UUID.randomUUID();
            User user1 = createUser(id1, "ana", "ana@mail.com");
            User user2 = createUser(id2, "luis", "luis@mail.com");

            when(userRepository.findById(id1))
                    .thenReturn(Optional.of(user1));
            when(userRepository.findById(id2))
                    .thenReturn(Optional.of(user2));

            Map<UUID, UserQueryUseCase.UserInfo> result =
                    userQueryService.findByIds(List.of(id1, id2));

            assertThat(result).hasSize(2);
            assertThat(result.get(id1).username()).isEqualTo("ana");
            assertThat(result.get(id2).username()).isEqualTo("luis");
        }

        @Test
        @DisplayName("Should return empty map for empty input")
        void shouldReturnEmptyMapForEmptyInput() {
            Map<UUID, UserQueryUseCase.UserInfo> result =
                    userQueryService.findByIds(List.of());

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should return empty map for null input")
        void shouldReturnEmptyMapForNullInput() {
            Map<UUID, UserQueryUseCase.UserInfo> result =
                    userQueryService.findByIds(null);

            assertThat(result).isEmpty();
        }
    }
}