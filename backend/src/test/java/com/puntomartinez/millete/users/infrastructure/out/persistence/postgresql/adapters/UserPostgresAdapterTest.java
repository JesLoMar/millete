package com.puntomartinez.millete.users.infrastructure.out.persistence.postgresql.adapters;

import com.puntomartinez.millete.users.domain.model.User;
import com.puntomartinez.millete.users.infrastructure.out.persistence.postgresql.entity.UserEntity;
import com.puntomartinez.millete.users.infrastructure.out.persistence.postgresql.mappers.UserEntityMapper;
import com.puntomartinez.millete.users.infrastructure.out.persistence.postgresql.repository.JpaUserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserPostgresAdapter")
class UserPostgresAdapterTest {

    @Mock
    private JpaUserRepository jpaUserRepository;

    @Mock
    private UserEntityMapper mapper;

    @InjectMocks
    private UserPostgresAdapter adapter;

    private UserEntity userEntity() {
        UserEntity entity = new UserEntity();
        entity.setId(UUID.randomUUID());
        entity.setUsername("ana");
        entity.setEmail("ana@mail.com");
        entity.setPassword("hashed");
        entity.setCreatedAt(LocalDateTime.now());
        entity.setModifiedAt(LocalDateTime.now());
        entity.setActive(true);
        entity.setAnonymized(false);
        return entity;
    }

    @Test
    @DisplayName("Should map domain to entity, save, and map back")
    void shouldSaveUser() {
        User domain = new User(
                UUID.randomUUID(), "ana", "ana@mail.com", "hashed",
                LocalDateTime.now(), LocalDateTime.now(), true, false
        );
        UserEntity entity = userEntity();
        UserEntity savedEntity = userEntity();
        User savedDomain = domain;

        when(mapper.toEntity(domain)).thenReturn(entity);
        when(jpaUserRepository.save(entity)).thenReturn(savedEntity);
        when(mapper.toDomain(savedEntity)).thenReturn(savedDomain);

        User result = adapter.save(domain);

        assertThat(result).isSameAs(savedDomain);
        verify(mapper).toEntity(domain);
        verify(jpaUserRepository).save(entity);
        verify(mapper).toDomain(savedEntity);
    }

    @Test
    @DisplayName("Should find by email and map to domain")
    void shouldFindByEmail() {
        UserEntity entity = userEntity();
        User domain = new User(
                entity.getId(), entity.getUsername(), entity.getEmail(),
                entity.getPassword(), entity.getCreatedAt(),
                entity.getModifiedAt(), entity.isActive(),
                entity.isAnonymized()
        );

        when(jpaUserRepository.findByEmail("ana@mail.com"))
                .thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(domain);

        Optional<User> result = adapter.findByEmail("ana@mail.com");

        assertThat(result).contains(domain);
    }

    @Test
    @DisplayName("Should find by username and map to domain")
    void shouldFindByUsername() {
        UserEntity entity = userEntity();
        User domain = new User(
                entity.getId(), entity.getUsername(), entity.getEmail(),
                entity.getPassword(), entity.getCreatedAt(),
                entity.getModifiedAt(), entity.isActive(),
                entity.isAnonymized()
        );

        when(jpaUserRepository.findByUsername("ana"))
                .thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(domain);

        Optional<User> result = adapter.findByUsername("ana");

        assertThat(result).contains(domain);
    }

    @Test
    @DisplayName("Should find by identifier trying username first then email")
    void shouldFindByIdentifierTryingUsernameFirst() {
        UserEntity entity = userEntity();
        User domain = new User(
                entity.getId(), entity.getUsername(), entity.getEmail(),
                entity.getPassword(), entity.getCreatedAt(),
                entity.getModifiedAt(), entity.isActive(),
                entity.isAnonymized()
        );

        when(jpaUserRepository.findByUsername("ana"))
                .thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(domain);

        Optional<User> result = adapter.findByIdentifier("ana");

        assertThat(result).contains(domain);
    }

    @Test
    @DisplayName("Should return empty when identifier not found")
    void shouldReturnEmptyWhenIdentifierNotFound() {
        when(jpaUserRepository.findByUsername("unknown"))
                .thenReturn(Optional.empty());
        when(jpaUserRepository.findByEmail("unknown"))
                .thenReturn(Optional.empty());

        Optional<User> result = adapter.findByIdentifier("unknown");

        assertThat(result).isEmpty();
    }
}