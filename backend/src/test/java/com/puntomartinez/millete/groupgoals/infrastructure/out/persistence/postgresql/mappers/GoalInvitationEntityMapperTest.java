package com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.mappers;

import com.puntomartinez.millete.groupgoals.domain.model.GoalInvitation;
import com.puntomartinez.millete.groupgoals.domain.model.InvitationStatus;
import com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.entity.GoalInvitationEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GoalInvitationEntityMapper")
class GoalInvitationEntityMapperTest {

    private final GoalInvitationEntityMapper mapper =
            Mappers.getMapper(GoalInvitationEntityMapper.class);

    @Test
    @DisplayName("Should map domain to entity")
    void shouldMapDomainToEntity() {
        GoalInvitation domain = GoalInvitation.create(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()
        );

        GoalInvitationEntity entity = mapper.toEntity(domain);

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(domain.getId());
        assertThat(entity.getStatus()).isEqualTo("PENDING");
        assertThat(entity.isActive()).isTrue();
    }

    @Test
    @DisplayName("Should map entity to domain")
    void shouldMapEntityToDomain() {
        GoalInvitationEntity entity = new GoalInvitationEntity();
        entity.setId(UUID.randomUUID());
        entity.setGoalId(UUID.randomUUID());
        entity.setInviterUserId(UUID.randomUUID());
        entity.setInvitedUserId(UUID.randomUUID());
        entity.setStatus("PENDING");
        entity.setExpiresAt(LocalDateTime.now().plusDays(7));
        entity.setCreatedAt(LocalDateTime.now());
        entity.setModifiedAt(LocalDateTime.now());
        entity.setActive(true);

        GoalInvitation domain = mapper.toDomain(entity);

        assertThat(domain).isNotNull();
        assertThat(domain.getStatus()).isEqualTo(InvitationStatus.PENDING);
        assertThat(domain.isActive()).isTrue();
    }

    @Test
    @DisplayName("Should return null when entity is null")
    void shouldReturnNullWhenEntityIsNull() {
        assertThat(mapper.toDomain(null)).isNull();
    }

    @Test
    @DisplayName("Should preserve data in domain-entity-domain round trip")
    void shouldPreserveDataInRoundTrip() {
        GoalInvitation original = GoalInvitation.create(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()
        );

        GoalInvitationEntity entity = mapper.toEntity(original);
        GoalInvitation restored = mapper.toDomain(entity);

        assertThat(restored.getId()).isEqualTo(original.getId());
        assertThat(restored.getGoalId()).isEqualTo(original.getGoalId());
        assertThat(restored.getInviterUserId())
                .isEqualTo(original.getInviterUserId());
        assertThat(restored.getInvitedUserId())
                .isEqualTo(original.getInvitedUserId());
        assertThat(restored.getStatus()).isEqualTo(original.getStatus());
        assertThat(restored.isActive()).isEqualTo(original.isActive());
    }
}