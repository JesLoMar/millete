package com.puntomartinez.millete.users.infrastructure.out.persistence.postgresql.mappers;

import com.puntomartinez.millete.users.domain.model.UserLoginSecurity;
import com.puntomartinez.millete.users.infrastructure.out.persistence.postgresql.entity.UserLoginSecurityEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UserLoginSecurityEntityMapper")
class UserLoginSecurityEntityMapperTest {

    private final UserLoginSecurityEntityMapper mapper =
            Mappers.getMapper(UserLoginSecurityEntityMapper.class);

    @Test
    @DisplayName("Should map domain to entity")
    void shouldMapDomainToEntity() {
        UserLoginSecurity domain = new UserLoginSecurity();
        domain.setUserId(UUID.randomUUID());
        domain.setFailedAttempts(3);
        domain.setBlockedUntil(Instant.now().plusSeconds(900));
        domain.setCreatedAt(Instant.now());
        domain.setModifiedAt(Instant.now());

        UserLoginSecurityEntity entity = mapper.toEntity(domain);

        assertThat(entity).isNotNull();
        assertThat(entity.getUserId()).isEqualTo(domain.getUserId());
        assertThat(entity.getFailedAttempts()).isEqualTo(3);
        assertThat(entity.getBlockedUntil()).isEqualTo(domain.getBlockedUntil());
    }

    @Test
    @DisplayName("Should map entity to domain")
    void shouldMapEntityToDomain() {
        UserLoginSecurityEntity entity = new UserLoginSecurityEntity();
        entity.setUserId(UUID.randomUUID());
        entity.setFailedAttempts(2);
        entity.setCreatedAt(Instant.now());
        entity.setModifiedAt(Instant.now());

        UserLoginSecurity domain = mapper.toDomain(entity);

        assertThat(domain).isNotNull();
        assertThat(domain.getUserId()).isEqualTo(entity.getUserId());
        assertThat(domain.getFailedAttempts()).isEqualTo(2);
    }
}