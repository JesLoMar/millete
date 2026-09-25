package com.puntomartinez.millete.users.infrastructure.out.persistence.postgresql.mappers;

import com.puntomartinez.millete.users.domain.model.UserSession;
import com.puntomartinez.millete.users.infrastructure.out.persistence.postgresql.entity.UserSessionEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UserSessionEntityMapper")
class UserSessionEntityMapperTest {

    private final UserSessionEntityMapper mapper =
            Mappers.getMapper(UserSessionEntityMapper.class);

    @Test
    @DisplayName("Should map domain to entity")
    void shouldMapDomainToEntity() {
        UserSession domain = new UserSession();
        domain.setId(UUID.randomUUID());
        domain.setUserId(UUID.randomUUID());
        domain.setChannel("WEB");
        domain.setActive(true);
        domain.setCreatedAt(Instant.now());
        domain.setModifiedAt(Instant.now());

        UserSessionEntity entity = mapper.toEntity(domain);

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(domain.getId());
        assertThat(entity.getUserId()).isEqualTo(domain.getUserId());
        assertThat(entity.getChannel()).isEqualTo("WEB");
        assertThat(entity.isActive()).isTrue();
    }

    @Test
    @DisplayName("Should map entity to domain")
    void shouldMapEntityToDomain() {
        UserSessionEntity entity = new UserSessionEntity();
        entity.setId(UUID.randomUUID());
        entity.setUserId(UUID.randomUUID());
        entity.setChannel("WEB");
        entity.setActive(true);
        entity.setCreatedAt(Instant.now());
        entity.setModifiedAt(Instant.now());

        UserSession domain = mapper.toDomain(entity);

        assertThat(domain).isNotNull();
        assertThat(domain.getId()).isEqualTo(entity.getId());
        assertThat(domain.getUserId()).isEqualTo(entity.getUserId());
        assertThat(domain.getChannel()).isEqualTo("WEB");
        assertThat(domain.isActive()).isTrue();
    }
}