package com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.mappers;

import com.puntomartinez.millete.groupgoals.domain.model.GoalMember;
import com.puntomartinez.millete.groupgoals.domain.model.GoalRole;
import com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.entity.GoalMemberEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GoalMemberEntityMapper")
class GoalMemberEntityMapperTest {

    private final GoalMemberEntityMapper mapper =
            Mappers.getMapper(GoalMemberEntityMapper.class);

    @Test
    @DisplayName("Should map domain to entity")
    void shouldMapDomainToEntity() {
        GoalMember domain = GoalMember.create(
                UUID.randomUUID(), UUID.randomUUID(),
                GoalRole.ADMIN, new BigDecimal("2000.00")
        );

        GoalMemberEntity entity = mapper.toEntity(domain);

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(domain.getId());
        assertThat(entity.getRole()).isEqualTo("ADMIN");
        assertThat(entity.getSalary()).isEqualByComparingTo("2000.00");
        assertThat(entity.isActive()).isTrue();
    }

    @Test
    @DisplayName("Should map entity to domain")
    void shouldMapEntityToDomain() {
        GoalMemberEntity entity = new GoalMemberEntity();
        entity.setId(UUID.randomUUID());
        entity.setGoalId(UUID.randomUUID());
        entity.setUserId(UUID.randomUUID());
        entity.setRole("ADMIN");
        entity.setSalary(new BigDecimal("2000.00"));
        entity.setJoinedAt(LocalDateTime.now());
        entity.setCreatedAt(LocalDateTime.now());
        entity.setModifiedAt(LocalDateTime.now());
        entity.setActive(true);

        GoalMember domain = mapper.toDomain(entity);

        assertThat(domain).isNotNull();
        assertThat(domain.getRole()).isEqualTo(GoalRole.ADMIN);
        assertThat(domain.getSalary()).isEqualByComparingTo("2000.00");
        assertThat(domain.isAdmin()).isTrue();
        assertThat(domain.isActive()).isTrue();
    }

    @Test
    @DisplayName("Should preserve data in domain-entity-domain round trip")
    void shouldPreserveDataInRoundTrip() {
        GoalMember original = GoalMember.create(
                UUID.randomUUID(), UUID.randomUUID(),
                GoalRole.MEMBER, new BigDecimal("1500.00"),
                new BigDecimal("25.00")
        );

        GoalMemberEntity entity = mapper.toEntity(original);
        GoalMember restored = mapper.toDomain(entity);

        assertThat(restored.getId()).isEqualTo(original.getId());
        assertThat(restored.getGoalId()).isEqualTo(original.getGoalId());
        assertThat(restored.getUserId()).isEqualTo(original.getUserId());
        assertThat(restored.getRole()).isEqualTo(original.getRole());
        assertThat(restored.getSalary())
                .isEqualByComparingTo(original.getSalary());
        assertThat(restored.getCustomPercentage())
                .isEqualByComparingTo(original.getCustomPercentage());
        assertThat(restored.isActive()).isEqualTo(original.isActive());
    }
}