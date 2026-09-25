package com.puntomartinez.millete.users.infrastructure.out.persistence.postgresql.mappers;

import com.puntomartinez.millete.users.domain.model.User;
import com.puntomartinez.millete.users.infrastructure.out.persistence.postgresql.entity.UserEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UserEntityMapper")
class UserEntityMapperTest {

    private final UserEntityMapper mapper =
            Mappers.getMapper(UserEntityMapper.class);

    @Test
    @DisplayName("Should map domain to entity")
    void shouldMapDomainToEntity() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime modifiedAt = LocalDateTime.of(2024, 1, 2, 10, 0);

        User domain = new User(
                id, "ana", "ana@mail.com", "hashed",
                createdAt, modifiedAt, true, false
        );

        UserEntity entity = mapper.toEntity(domain);

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getUsername()).isEqualTo("ana");
        assertThat(entity.getEmail()).isEqualTo("ana@mail.com");
        assertThat(entity.getPassword()).isEqualTo("hashed");
        assertThat(entity.getCreatedAt()).isEqualTo(createdAt);
        assertThat(entity.getModifiedAt()).isEqualTo(modifiedAt);
        assertThat(entity.isActive()).isTrue();
        assertThat(entity.isAnonymized()).isFalse();
    }

    @Test
    @DisplayName("Should map entity to domain")
    void shouldMapEntityToDomain() {
        UUID id = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime modifiedAt = LocalDateTime.of(2024, 1, 2, 10, 0);

        UserEntity entity = new UserEntity();
        entity.setId(id);
        entity.setUsername("ana");
        entity.setEmail("ana@mail.com");
        entity.setPassword("hashed");
        entity.setCreatedAt(createdAt);
        entity.setModifiedAt(modifiedAt);
        entity.setActive(true);
        entity.setAnonymized(false);

        User domain = mapper.toDomain(entity);

        assertThat(domain).isNotNull();
        assertThat(domain.getId()).isEqualTo(id);
        assertThat(domain.getUsername()).isEqualTo("ana");
        assertThat(domain.getEmail()).isEqualTo("ana@mail.com");
        assertThat(domain.isActive()).isTrue();
    }

    @Test
    @DisplayName("Should preserve data in domain-entity-domain round trip")
    void shouldPreserveDataInRoundTrip() {
        User original = new User(
                UUID.randomUUID(), "ana", "ana@mail.com", "hashed",
                Instant.now(), Instant.now(), true, false
        );

        UserEntity entity = mapper.toEntity(original);
        User restored = mapper.toDomain(entity);

        assertThat(restored.getId()).isEqualTo(original.getId());
        assertThat(restored.getUsername()).isEqualTo(original.getUsername());
        assertThat(restored.getEmail()).isEqualTo(original.getEmail());
        assertThat(restored.getPassword()).isEqualTo(original.getPassword());
        assertThat(restored.isActive()).isEqualTo(original.isActive());
        assertThat(restored.isAnonymized()).isEqualTo(original.isAnonymized());
    }
}