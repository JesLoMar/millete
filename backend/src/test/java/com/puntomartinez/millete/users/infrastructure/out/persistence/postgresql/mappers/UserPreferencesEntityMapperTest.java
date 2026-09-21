package com.puntomartinez.millete.users.infrastructure.out.persistence.postgresql.mappers;

import com.puntomartinez.millete.users.domain.model.UserPreferences;
import com.puntomartinez.millete.users.infrastructure.out.persistence.postgresql.entity.UserPreferencesEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UserPreferencesEntityMapper")
class UserPreferencesEntityMapperTest {

    private final UserPreferencesEntityMapper mapper =
            Mappers.getMapper(UserPreferencesEntityMapper.class);

    @Test
    @DisplayName("Should map domain to entity")
    void shouldMapDomainToEntity() {
        UserPreferences domain = new UserPreferences();
        domain.setId(UUID.randomUUID());
        domain.setUserId(UUID.randomUUID());
        domain.setPreferences(Map.of("theme", "dark"));
        domain.setCreatedAt(LocalDateTime.now());
        domain.setModifiedAt(LocalDateTime.now());

        UserPreferencesEntity entity = mapper.toEntity(domain);

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(domain.getId());
        assertThat(entity.getUserId()).isEqualTo(domain.getUserId());
        assertThat(entity.getPreferences()).containsEntry("theme", "dark");
    }

    @Test
    @DisplayName("Should map entity to domain")
    void shouldMapEntityToDomain() {
        UserPreferencesEntity entity = new UserPreferencesEntity();
        entity.setId(UUID.randomUUID());
        entity.setUserId(UUID.randomUUID());
        entity.setPreferences(Map.of("theme", "dark"));
        entity.setCreatedAt(LocalDateTime.now());
        entity.setModifiedAt(LocalDateTime.now());

        UserPreferences domain = mapper.toDomain(entity);

        assertThat(domain).isNotNull();
        assertThat(domain.getId()).isEqualTo(entity.getId());
        assertThat(domain.getUserId()).isEqualTo(entity.getUserId());
        assertThat(domain.getPreferences()).containsEntry("theme", "dark");
    }
}